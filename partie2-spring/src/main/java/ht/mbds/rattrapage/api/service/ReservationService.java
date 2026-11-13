package ht.mbds.rattrapage.api.service;

import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.domaine.TypeTarif;
import ht.mbds.rattrapage.api.dto.ReservationRequest;
import ht.mbds.rattrapage.api.dto.ReservationResponse;
import ht.mbds.rattrapage.api.entite.EvenementEntity;
import ht.mbds.rattrapage.api.entite.ReservationEntity;
import ht.mbds.rattrapage.api.exception.ConflitException;
import ht.mbds.rattrapage.api.exception.RequeteInvalideException;
import ht.mbds.rattrapage.api.exception.RessourceIntrouvableException;
import ht.mbds.rattrapage.api.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EvenementService evenementService;

    public ReservationService(ReservationRepository reservationRepository, EvenementService evenementService) {
        this.reservationRepository = reservationRepository;
        this.evenementService = evenementService;
    }

    @Transactional
    public ReservationResponse reserver(String codeEvenement, ReservationRequest requete) {
        verifierTarif(requete);
        EvenementEntity evenement = evenementService.chargerEvenement(codeEvenement);

        String email = requete.email().trim().toLowerCase(Locale.ROOT);
        if (reservationRepository.compterReservations(codeEvenement, email, StatutReservation.ANNULEE) > 0) {
            throw new ConflitException("Reservation deja existante pour " + email + " sur " + codeEvenement);
        }

        TypeTarif tarif = requete.tarif();
        Integer pourcentage = tarif == TypeTarif.REDUIT ? requete.pourcentage() : null;
        Integer nombrePersonnes = tarif == TypeTarif.GROUPE ? requete.nombrePersonnes() : null;
        int places = tarif.places(nombrePersonnes);
        BigDecimal montant = tarif.montant(evenement.getPrixBase(), pourcentage, nombrePersonnes);

        // Un groupe n'est jamais confirme partiellement
        StatutReservation statut = places <= evenementService.placesRestantes(evenement)
                ? StatutReservation.CONFIRMEE
                : StatutReservation.EN_ATTENTE;

        ReservationEntity reservation = new ReservationEntity(evenement, email, requete.nom().trim(), tarif,
                pourcentage, nombrePersonnes, places, montant, statut);
        return versReponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse trouver(Long id) {
        return versReponse(charger(id));
    }

    @Transactional
    public ReservationResponse annuler(Long id) {
        ReservationEntity reservation = charger(id);
        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new ConflitException("Reservation deja annulee : " + id);
        }
        reservation.setStatut(StatutReservation.ANNULEE);
        reservationRepository.saveAndFlush(reservation);

        promouvoirListeAttente(reservation.getEvenement());
        return versReponse(reservation);
    }

    /**
     * Liste d'attente parcourue par identifiant croissant : chaque reservation qui tient
     * dans les places libres est confirmee ; les autres restent en attente sans bloquer les suivantes.
     */
    private void promouvoirListeAttente(EvenementEntity evenement) {
        int libres = evenementService.placesRestantes(evenement);
        for (ReservationEntity enAttente : reservationRepository
                .findByEvenementCodeAndStatutOrderByIdAsc(evenement.getCode(), StatutReservation.EN_ATTENTE)) {
            if (enAttente.getPlaces() <= libres) {
                enAttente.setStatut(StatutReservation.CONFIRMEE);
                reservationRepository.save(enAttente);
                libres -= enAttente.getPlaces();
            }
        }
    }

    /** Regles conditionnelles : pourcentage obligatoire si REDUIT, nombrePersonnes si GROUPE. */
    void verifierTarif(ReservationRequest requete) {
        if (requete.tarif() == TypeTarif.REDUIT) {
            Integer p = requete.pourcentage();
            if (p == null || p < 1 || p > 100) {
                throw new RequeteInvalideException(Map.of("pourcentage",
                        "obligatoire pour le tarif REDUIT, entre 1 et 100"));
            }
        }
        if (requete.tarif() == TypeTarif.GROUPE) {
            Integer n = requete.nombrePersonnes();
            if (n == null || n < 5 || n > 50) {
                throw new RequeteInvalideException(Map.of("nombrePersonnes",
                        "obligatoire pour le tarif GROUPE, entre 5 et 50"));
            }
        }
    }

    private ReservationEntity charger(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Reservation inconnue : " + id));
    }

    private ReservationResponse versReponse(ReservationEntity r) {
        return new ReservationResponse(r.getId(), r.getEvenement().getCode(), r.getEmail(), r.getNom(),
                r.getTarif(), r.getPlaces(), r.getMontant().setScale(2, RoundingMode.HALF_UP), r.getStatut());
    }
}
