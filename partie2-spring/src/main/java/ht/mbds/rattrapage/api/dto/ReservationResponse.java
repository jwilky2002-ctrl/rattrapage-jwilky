package ht.mbds.rattrapage.api.dto;

import ht.mbds.rattrapage.api.domaine.StatutReservation;
import ht.mbds.rattrapage.api.domaine.TypeTarif;

import java.math.BigDecimal;

public record ReservationResponse(Long id, String codeEvenement, String email, String nom, TypeTarif tarif,
                                  int places, BigDecimal montant, StatutReservation statut) {
}
