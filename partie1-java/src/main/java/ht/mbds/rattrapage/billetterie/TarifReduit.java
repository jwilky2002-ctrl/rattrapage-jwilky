package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TarifReduit(int pourcentage) implements Tarif {

    public TarifReduit {
        if (pourcentage < 1 || pourcentage > 100) {
            throw new IllegalArgumentException("Le pourcentage doit etre entre 1 et 100");
        }
    }

    @Override
    public BigDecimal appliquer(BigDecimal prixBase) {
        // prix x (100 - pourcentage) / 100, arrondi a 2 decimales
        return prixBase.multiply(BigDecimal.valueOf(100L - pourcentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public int places() {
        return 1;
    }
}
