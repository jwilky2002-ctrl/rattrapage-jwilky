package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record TarifPlein() implements Tarif {

    @Override
    public BigDecimal appliquer(BigDecimal prixBase) {
        return prixBase.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int places() {
        return 1;
    }
}
