package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;

public record Reservation(String id, Evenement evenement, Participant participant, Tarif tarif, BigDecimal montant,
                          StatutReservation statut) {

    public Reservation avecStatut(StatutReservation nouveauStatut) {
        // Record immuable : on renvoie une copie, l'objet d'origine n'est pas modifie
        return new Reservation(id, evenement, participant, tarif, montant, nouveauStatut);
    }
}
