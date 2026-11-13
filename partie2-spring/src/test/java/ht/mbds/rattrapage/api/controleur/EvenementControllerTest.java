package ht.mbds.rattrapage.api.controleur;

import ht.mbds.rattrapage.api.config.SecurityConfig;
import ht.mbds.rattrapage.api.domaine.Categorie;
import ht.mbds.rattrapage.api.dto.EvenementRequest;
import ht.mbds.rattrapage.api.dto.EvenementResponse;
import ht.mbds.rattrapage.api.exception.RessourceIntrouvableException;
import ht.mbds.rattrapage.api.service.EvenementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvenementController.class)
@Import(SecurityConfig.class)
class EvenementControllerTest {

    private static final String JSON_VALIDE = """
            { "code": "CON-001", "titre": "Kompa Live", "categorie": "CONCERT",
              "date": "2030-06-15", "capacite": 500, "prixBase": 25.00 }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvenementService evenementService;

    private EvenementResponse reponse() {
        return new EvenementResponse("CON-001", "Kompa Live", Categorie.CONCERT, LocalDate.of(2030, 6, 15),
                500, new BigDecimal("25.00"), 500);
    }

    @Test
    void creationParAdmin() throws Exception {
        when(evenementService.creer(any(EvenementRequest.class))).thenReturn(reponse());

        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDE))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/evenements/CON-001"))
                .andExpect(jsonPath("$.code").value("CON-001"))
                .andExpect(jsonPath("$.date").value("2030-06-15"))
                .andExpect(jsonPath("$.placesRestantes").value(500));
    }

    @Test
    void creationSansAuthentification401() throws Exception {
        mockMvc.perform(post("/api/evenements").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creationMauvaisMotDePasse401() throws Exception {
        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "faux"))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void creationParAgent403() throws Exception {
        mockMvc.perform(post("/api/evenements").with(httpBasic("agent", "agent123"))
                        .contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDE))
                .andExpect(status().isForbidden());
    }

    @Test
    void validation400() throws Exception {
        String invalide = """
                { "code": "CON-001", "titre": "Kompa Live", "categorie": "CONCERT",
                  "date": "2030-06-15", "capacite": 0, "prixBase": 25.00 }
                """;
        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(invalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Requete invalide"))
                .andExpect(jsonPath("$.erreurs.capacite").exists());
    }

    @Test
    void categorieInconnue400() throws Exception {
        String invalide = JSON_VALIDE.replace("CONCERT", "OPERA");
        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content(invalide))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void consultationPublique() throws Exception {
        when(evenementService.trouver("CON-001")).thenReturn(reponse());

        mockMvc.perform(get("/api/evenements/CON-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre").value("Kompa Live"))
                .andExpect(jsonPath("$.prixBase").value(25.00));
    }

    @Test
    void evenementInconnu404() throws Exception {
        when(evenementService.trouver("CON-999"))
                .thenThrow(new RessourceIntrouvableException("Evenement inconnu : CON-999"));

        mockMvc.perform(get("/api/evenements/CON-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Evenement inconnu : CON-999"));
    }
}
