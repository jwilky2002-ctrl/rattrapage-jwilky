package ht.mbds.rattrapage.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErreurResponse(int status, String message, Map<String, String> erreurs) {

    public ErreurResponse(int status, String message) {
        this(status, message, null);
    }
}
