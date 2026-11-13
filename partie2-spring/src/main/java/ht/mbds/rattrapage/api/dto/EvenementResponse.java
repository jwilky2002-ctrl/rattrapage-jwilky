package ht.mbds.rattrapage.api.dto;

import ht.mbds.rattrapage.api.domaine.Categorie;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvenementResponse(String code, String titre, Categorie categorie, LocalDate date, int capacite,
                                BigDecimal prixBase, int placesRestantes) {
}
