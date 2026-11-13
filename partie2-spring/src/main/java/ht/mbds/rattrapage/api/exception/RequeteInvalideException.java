package ht.mbds.rattrapage.api.exception;

import java.util.Map;

/** 400 avec le detail des champs en erreur. */
public class RequeteInvalideException extends RuntimeException {

    private final transient Map<String, String> erreurs;

    public RequeteInvalideException(Map<String, String> erreurs) {
        super("Requete invalide");
        this.erreurs = erreurs;
    }

    public Map<String, String> getErreurs() {
        return erreurs;
    }
}
