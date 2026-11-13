package ht.mbds.rattrapage.billetterie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.regex.Pattern;

public record Evenement(String code, String titre, Categorie categorie, LocalDate date, int capacite, BigDecimal prixBase) {

    // 3 lettres majuscules, un tiret, 3 chiffres (ex. CON-001)
    private static final Pattern FORMAT_CODE = Pattern.compile("^[A-Z]{3}-[0-9]{3}$");

    public Evenement {
        if (code == null || !FORMAT_CODE.matcher(code).matches()) {
            throw new IllegalArgumentException("Code invalide : " + code);
        }
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (categorie == null) {
            throw new IllegalArgumentException("La categorie est obligatoire");
        }
        if (date == null) {
            throw new IllegalArgumentException("La date est obligatoire");
        }
        if (capacite <= 0) {
            throw new IllegalArgumentException("La capacite doit etre strictement positive");
        }
        if (prixBase == null || prixBase.signum() < 0) {
            throw new IllegalArgumentException("Le prix doit etre positif ou nul");
        }
        titre = titre.trim();
        prixBase = prixBase.setScale(2, RoundingMode.HALF_UP);
    }
}
