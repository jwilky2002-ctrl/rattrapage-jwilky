package ht.mbds.rattrapage.api.service;

import ht.mbds.rattrapage.api.domaine.Categorie;
import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.dto.EvenementRequest;
import ht.mbds.rattrapage.api.dto.EvenementResponse;
import ht.mbds.rattrapage.api.dto.PageResponse;
import ht.mbds.rattrapage.api.entite.EvenementEntity;
import ht.mbds.rattrapage.api.exception.ConflitException;
import ht.mbds.rattrapage.api.exception.RequeteInvalideException;
import ht.mbds.rattrapage.api.exception.RessourceIntrouvableException;
import ht.mbds.rattrapage.api.repository.EvenementRepository;
import ht.mbds.rattrapage.api.repository.ReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EvenementService {

    private final EvenementRepository evenementRepository;
    private final ReservationRepository reservationRepository;

    public EvenementService(EvenementRepository evenementRepository, ReservationRepository reservationRepository) {
        this.evenementRepository = evenementRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public EvenementResponse creer(EvenementRequest requete) {
        if (evenementRepository.existsById(requete.code())) {
            throw new ConflitException("Evenement deja existant : " + requete.code());
        }
        EvenementEntity evenement = new EvenementEntity(
                requete.code(),
                requete.titre().trim(),
                requete.categorie(),
                requete.date(),
                requete.capacite(),
                requete.prixBase().setScale(2, RoundingMode.HALF_UP));
        return versReponse(evenementRepository.save(evenement));
    }

    @Transactional(readOnly = true)
    public PageResponse<EvenementResponse> lister(Categorie categorie, int page, int taille) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        if (page < 0) {
            erreurs.put("page", "doit etre superieur ou egal a 0");
        }
        if (taille < 1 || taille > 100) {
            erreurs.put("taille", "doit etre entre 1 et 100");
        }
        if (!erreurs.isEmpty()) {
            throw new RequeteInvalideException(erreurs);
        }

        // Tri par date croissante, puis par code
        Pageable pageable = PageRequest.of(page, taille, Sort.by("dateEvenement").ascending().and(Sort.by("code")));
        Page<EvenementEntity> resultat = categorie == null
                ? evenementRepository.findAll(pageable)
                : evenementRepository.findByCategorie(categorie, pageable);

        return new PageResponse<>(
                resultat.getContent().stream().map(this::versReponse).toList(),
                resultat.getNumber(),
                resultat.getSize(),
                resultat.getTotalElements(),
                resultat.getTotalPages());
    }

    @Transactional(readOnly = true)
    public EvenementResponse trouver(String code) {
        return versReponse(chargerEvenement(code));
    }

    EvenementEntity chargerEvenement(String code) {
        return evenementRepository.findById(code)
                .orElseThrow(() -> new RessourceIntrouvableException("Evenement inconnu : " + code));
    }

    int placesRestantes(EvenementEntity evenement) {
        long confirmees = reservationRepository.sommePlaces(evenement.getCode(), StatutReservation.CONFIRMEE);
        return evenement.getCapacite() - (int) confirmees;
    }

    private EvenementResponse versReponse(EvenementEntity e) {
        return new EvenementResponse(e.getCode(), e.getTitre(), e.getCategorie(), e.getDateEvenement(),
                e.getCapacite(), e.getPrixBase().setScale(2, RoundingMode.HALF_UP), placesRestantes(e));
    }
}
