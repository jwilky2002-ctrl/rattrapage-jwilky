package ht.mbds.rattrapage.api.exception;

import ht.mbds.rattrapage.api.dto.ErreurResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestion centralisee des erreurs : toujours { "status": ..., "message": ... }
 * et, pour la validation, un objet "erreurs" champ -> message.
 */
@RestControllerAdvice
public class GestionnaireErreurs {

    private static final String REQUETE_INVALIDE = "Requete invalide";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurResponse> validation(MethodArgumentNotValidException ex) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        for (FieldError erreur : ex.getBindingResult().getFieldErrors()) {
            erreurs.putIfAbsent(erreur.getField(), erreur.getDefaultMessage());
        }
        return reponse(HttpStatus.BAD_REQUEST, REQUETE_INVALIDE, erreurs);
    }

    @ExceptionHandler(RequeteInvalideException.class)
    public ResponseEntity<ErreurResponse> requeteInvalide(RequeteInvalideException ex) {
        return reponse(HttpStatus.BAD_REQUEST, REQUETE_INVALIDE, ex.getErreurs());
    }

    /** JSON illisible ou valeur d'enumeration inconnue dans le corps. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErreurResponse> corpsIllisible(HttpMessageNotReadableException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Corps de requete illisible ou valeur invalide", null);
    }

    /** Parametre mal type (page=abc, categorie inconnue, id non numerique...). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErreurResponse> parametreMalType(MethodArgumentTypeMismatchException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Parametre invalide : " + ex.getName(), null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErreurResponse> parametreManquant(MissingServletRequestParameterException ex) {
        return reponse(HttpStatus.BAD_REQUEST, "Parametre manquant : " + ex.getParameterName(), null);
    }

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurResponse> introuvable(RessourceIntrouvableException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErreurResponse> routeInconnue(NoResourceFoundException ex) {
        return reponse(HttpStatus.NOT_FOUND, "Ressource introuvable", null);
    }

    @ExceptionHandler(ConflitException.class)
    public ResponseEntity<ErreurResponse> conflit(ConflitException ex) {
        return reponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErreurResponse> methodeNonSupportee(HttpRequestMethodNotSupportedException ex) {
        return reponse(HttpStatus.METHOD_NOT_ALLOWED, "Methode non supportee", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErreurResponse> inattendue(Exception ex) {
        return reponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", null);
    }

    private ResponseEntity<ErreurResponse> reponse(HttpStatus statut, String message, Map<String, String> erreurs) {
        return ResponseEntity.status(statut).body(new ErreurResponse(statut.value(), message, erreurs));
    }
}
