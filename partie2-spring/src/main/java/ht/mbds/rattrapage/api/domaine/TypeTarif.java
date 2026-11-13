package ht.mbds.rattrapage.api.domaine;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Types de tarif et regles de calcul (memes regles que la partie 1).
 */
public enum TypeTarif {
    PLEIN,
    REDUIT,
    GROUPE;

    private static final BigDecimal COEFFICIENT_GROUPE = new BigDecimal("0.85");

    /** Montant a payer, arrondi a 2 decimales (HALF_UP). */
    public BigDecimal montant(BigDecimal prixBase, Integer pourcentage, Integer nombrePersonnes) {
        return switch (this) {
            case PLEIN -> prixBase.setScale(2, RoundingMode.HALF_UP);
            case REDUIT -> prixBase.multiply(BigDecimal.valueOf(100L - pourcentage))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case GROUPE -> prixBase.multiply(BigDecimal.valueOf(nombrePersonnes))
                    .multiply(COEFFICIENT_GROUPE)
                    .setScale(2, RoundingMode.HALF_UP);
        };
    }

    /** Nombre de places consommees. */
    public int places(Integer nombrePersonnes) {
        return this == GROUPE ? nombrePersonnes : 1;
    }
}
