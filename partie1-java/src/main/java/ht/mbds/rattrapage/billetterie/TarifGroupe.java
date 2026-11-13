package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TarifGroupe(int nombrePersonnes) implements Tarif {

    private static final BigDecimal COEFFICIENT_GROUPE = new BigDecimal("0.85");

    public TarifGroupe {
        if (nombrePersonnes < 5 || nombrePersonnes > 50) {
            throw new IllegalArgumentException("Le nombre de personnes doit etre entre 5 et 50");
        }
    }

    @Override
    public BigDecimal appliquer(BigDecimal prixBase) {
        // prix x nombre x 0,85, arrondi a 2 decimales
        return prixBase.multiply(BigDecimal.valueOf(nombrePersonnes))
                .multiply(COEFFICIENT_GROUPE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int places() {
        return nombrePersonnes;
    }
}
