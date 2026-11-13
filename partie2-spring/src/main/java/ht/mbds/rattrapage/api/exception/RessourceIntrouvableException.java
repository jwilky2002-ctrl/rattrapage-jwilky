package ht.mbds.rattrapage.api.exception;

/** 404 */
public class RessourceIntrouvableException extends RuntimeException {

    public RessourceIntrouvableException(String message) {
        super(message);
    }
}
