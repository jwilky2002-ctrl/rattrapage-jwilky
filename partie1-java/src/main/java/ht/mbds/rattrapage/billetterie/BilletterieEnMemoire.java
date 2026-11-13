package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class BilletterieEnMemoire implements Billetterie {

    // Ordre d'insertion conserve = ordre d'arrivee
    private final Map<String, Evenement> evenements = new LinkedHashMap<>();
    private final Map<String, Reservation> reservations = new LinkedHashMap<>();
    private int compteur = 0;

    @Override
    public void ajouterEvenement(Evenement evenement) {
        Objects.requireNonNull(evenement, "evenement");
        if (evenements.containsKey(evenement.code())) {
            throw new EvenementDejaExistantException(evenement.code());
        }
        evenements.put(evenement.code(), evenement);
    }

    @Override
    public Reservation reserver(String codeEvenement, Participant participant, Tarif tarif) {
        Evenement evenement = trouverEvenement(codeEvenement);
        Objects.requireNonNull(participant, "participant");
        Objects.requireNonNull(tarif, "tarif");

        boolean dejaReserve = reservations.values().stream()
                .anyMatch(r -> r.evenement().code().equals(codeEvenement)
                        && r.participant().email().equalsIgnoreCase(participant.email())
                        && r.statut() != StatutReservation.ANNULEE);
        if (dejaReserve) {
            throw new ReservationDupliqueeException(participant.email(), codeEvenement);
        }

        // Un groupe n'est jamais confirme partiellement
        StatutReservation statut = tarif.places() <= placesRestantes(codeEvenement)
                ? StatutReservation.CONFIRMEE
                : StatutReservation.EN_ATTENTE;

        compteur++;
        String id = String.format("R-%04d", compteur);
        Reservation reservation = new Reservation(id, evenement, participant, tarif,
                tarif.appliquer(evenement.prixBase()), statut);
        reservations.put(id, reservation);
        return reservation;
    }

    @Override
    public Reservation annuler(String idReservation) {
        Reservation reservation = reservations.get(idReservation);
        if (reservation == null) {
            throw new ReservationInconnueException(idReservation);
        }
        if (reservation.statut() == StatutReservation.ANNULEE) {
            throw new IllegalStateException("Reservation deja annulee : " + idReservation);
        }
        Reservation annulee = reservation.avecStatut(StatutReservation.ANNULEE);
        reservations.put(idReservation, annulee);

        promouvoirListeAttente(reservation.evenement().code());
        return annulee;
    }

    /**
     * Parcourt la liste d'attente dans l'ordre d'arrivee : chaque reservation qui tient
     * dans les places libres est confirmee, les autres restent en attente sans bloquer les suivantes.
     */
    private void promouvoirListeAttente(String codeEvenement) {
        int libres = placesRestantes(codeEvenement);
        for (Reservation enAttente : listeAttente(codeEvenement)) {
            if (enAttente.tarif().places() <= libres) {
                reservations.put(enAttente.id(), enAttente.avecStatut(StatutReservation.CONFIRMEE));
                libres -= enAttente.tarif().places();
            }
        }
    }

    @Override
    public int placesRestantes(String codeEvenement) {
        Evenement evenement = trouverEvenement(codeEvenement);
        return evenement.capacite() - placesConfirmees(codeEvenement);
    }

    private int placesConfirmees(String codeEvenement) {
        return reservations.values().stream()
                .filter(r -> r.evenement().code().equals(codeEvenement))
                .filter(r -> r.statut() == StatutReservation.CONFIRMEE)
                .mapToInt(r -> r.tarif().places())
                .sum();
    }

    @Override
    public List<Reservation> reservationsDe(String email) {
        if (email == null) {
            return List.of();
        }
        String recherche = email.trim();
        return reservations.values().stream()
                .filter(r -> r.participant().email().equalsIgnoreCase(recherche))
                .sorted(Comparator.comparing((Reservation r) -> r.evenement().date())
                        .thenComparing(Reservation::id))
                .toList();
    }

    @Override
    public List<Reservation> listeAttente(String codeEvenement) {
        trouverEvenement(codeEvenement);
        return reservations.values().stream()
                .filter(r -> r.evenement().code().equals(codeEvenement))
                .filter(r -> r.statut() == StatutReservation.EN_ATTENTE)
                .sorted(Comparator.comparing(Reservation::id))
                .toList();
    }

    @Override
    public Map<Categorie, BigDecimal> chiffreAffairesParCategorie() {
        Map<Categorie, BigDecimal> resultat = new EnumMap<>(Categorie.class);
        for (Reservation r : reservations.values()) {
            if (r.statut() == StatutReservation.CONFIRMEE) {
                resultat.merge(r.evenement().categorie(), r.montant(), BigDecimal::add);
            }
        }
        return resultat;
    }

    @Override
    public Optional<Evenement> evenementLePlusRempli() {
        // Taux = confirmees / capacite, compare sans division :
        // a plus rempli que b  <=>  confA * capB > confB * capA
        Comparator<Evenement> parTauxDecroissant = (a, b) -> Long.compare(
                (long) placesConfirmees(b.code()) * a.capacite(),
                (long) placesConfirmees(a.code()) * b.capacite());

        return evenements.values().stream()
                .sorted(parTauxDecroissant
                        .thenComparing(Evenement::date)
                        .thenComparing(Evenement::code))
                .findFirst();
    }

    private Evenement trouverEvenement(String code) {
        Evenement evenement = code == null ? null : evenements.get(code);
        if (evenement == null) {
            throw new EvenementInconnuException(code);
        }
        return evenement;
    }
}
