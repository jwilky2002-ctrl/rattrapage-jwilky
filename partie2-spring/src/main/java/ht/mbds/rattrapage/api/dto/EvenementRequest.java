package ht.mbds.rattrapage.api.dto;

import ht.mbds.rattrapage.api.domaine.Categorie;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvenementRequest(

        @NotBlank(message = "est obligatoire")
        @Pattern(regexp = "[A-Z]{3}-[0-9]{3}", message = "doit respecter le format AAA-000")
        String code,

        @NotBlank(message = "est obligatoire")
        @Size(max = 100, message = "doit contenir 100 caracteres au plus")
        String titre,

        @NotNull(message = "est obligatoire")
        Categorie categorie,

        @NotNull(message = "est obligatoire")
        @FutureOrPresent(message = "doit etre aujourd'hui ou dans le futur")
        LocalDate date,

        @NotNull(message = "est obligatoire")
        @Min(value = 1, message = "doit etre superieur ou egal a 1")
        @Max(value = 10000, message = "doit etre inferieur ou egal a 10000")
        Integer capacite,

        @NotNull(message = "est obligatoire")
        @DecimalMin(value = "0.00", message = "doit etre positif ou nul")
        @Digits(integer = 10, fraction = 2, message = "doit avoir 2 decimales au plus")
        BigDecimal prixBase
) {
}
