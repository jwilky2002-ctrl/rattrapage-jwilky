package ht.mbds.rattrapage.api.dto;

import java.util.List;

public record PageResponse<T>(List<T> contenu, int page, int taille, long totalElements, int totalPages) {
}
