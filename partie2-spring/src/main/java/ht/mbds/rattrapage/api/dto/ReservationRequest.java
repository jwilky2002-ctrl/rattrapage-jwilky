package ht.mbds.rattrapage.api.dto;

import ht.mbds.rattrapage.api.domaine.TypeTarif;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Les regles conditionnelles (pourcentage si REDUIT, nombrePersonnes si GROUPE)
 * sont verifiees dans ReservationService.verifierTarif.
 */
public record ReservationRequest(

        @NotBlank(message = "est obligatoire")
        @Email(regexp = "^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$", message = "doit etre une adresse email valide")
        String email,

        @NotBlank(message = "est obligatoire")
        String nom,

        @NotNull(message = "est obligatoire")
        TypeTarif tarif,

        Integer pourcentage,

        Integer nombrePersonnes
) {
}
