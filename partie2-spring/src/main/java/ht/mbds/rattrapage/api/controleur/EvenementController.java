package ht.mbds.rattrapage.api.controleur;

import ht.mbds.rattrapage.api.domaine.Categorie;
import ht.mbds.rattrapage.api.dto.EvenementRequest;
import ht.mbds.rattrapage.api.dto.EvenementResponse;
import ht.mbds.rattrapage.api.dto.PageResponse;
import ht.mbds.rattrapage.api.service.EvenementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/evenements")
public class EvenementController {

    private final EvenementService evenementService;

    public EvenementController(EvenementService evenementService) {
        this.evenementService = evenementService;
    }

    /** Role ADMIN (voir SecurityConfig). */
    @PostMapping
    public ResponseEntity<EvenementResponse> creer(@Valid @RequestBody EvenementRequest requete) {
        EvenementResponse cree = evenementService.creer(requete);
        return ResponseEntity.created(URI.create("/api/evenements/" + cree.code())).body(cree);
    }

    @GetMapping
    public PageResponse<EvenementResponse> lister(@RequestParam(required = false) Categorie categorie,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int taille) {
        return evenementService.lister(categorie, page, taille);
    }

    @GetMapping("/{code}")
    public EvenementResponse trouver(@PathVariable String code) {
        return evenementService.trouver(code);
    }
}
