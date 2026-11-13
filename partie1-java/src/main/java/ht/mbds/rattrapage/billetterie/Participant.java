package ht.mbds.rattrapage.billetterie;

import java.util.Locale;
import java.util.regex.Pattern;

public record Participant(String email, String nom) {

    // texte@texte.texte : un seul @, au moins un point apres le @, aucun espace
    private static final Pattern FORMAT_EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    public Participant {
        if (email == null) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        email = email.trim();
        if (!FORMAT_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Email invalide : " + email);
        }
        email = email.toLowerCase(Locale.ROOT);

        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        nom = nom.trim();
    }
}
