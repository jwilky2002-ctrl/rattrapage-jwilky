package ht.mbds.rattrapage.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Scenario de bout en bout : creation, reservations, liste d'attente, annulation et promotion. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ScenarioCompletTest {

    @Autowired
    private MockMvc mockMvc;

    private String reserver(String email, String tarif, String extra) {
        return "{ \"email\": \"" + email + "\", \"nom\": \"Nom\", \"tarif\": \"" + tarif + "\"" + extra + " }";
    }

    @Test
    void scenario() throws Exception {
        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "THE-001", "titre": "  Piece  ", "categorie": "THEATRE",
                                  "date": "2030-01-10", "capacite": 6, "prixBase": 19.99 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titre").value("Piece"))
                .andExpect(jsonPath("$.placesRestantes").value(6));

        // Doublon de code -> 409
        mockMvc.perform(post("/api/evenements").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "code": "THE-001", "titre": "Autre", "categorie": "THEATRE",
                                  "date": "2030-01-10", "capacite": 6, "prixBase": 10 }
                                """))
                .andExpect(status().isConflict());

        // 1 place pleine -> CONFIRMEE
        String location = mockMvc.perform(post("/api/evenements/THE-001/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(reserver("Anne@Mail.HT", "PLEIN", "")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("anne@mail.ht"))
                .andExpect(jsonPath("$.montant").value(19.99))
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"))
                .andReturn().getResponse().getHeader("Location");

        // Groupe de 5 -> CONFIRMEE (0 place restante)
        mockMvc.perform(post("/api/evenements/THE-001/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(reserver("g@mail.ht", "GROUPE", ", \"nombrePersonnes\": 5")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montant").value(84.96))
                .andExpect(jsonPath("$.places").value(5))
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));

        // Reduit 15 % -> EN_ATTENTE (complet)
        mockMvc.perform(post("/api/evenements/THE-001/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(reserver("b@mail.ht", "REDUIT", ", \"pourcentage\": 15")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.montant").value(16.99))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"));

        // Doublon email (casse differente) -> 409
        mockMvc.perform(post("/api/evenements/THE-001/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(reserver("ANNE@mail.ht", "PLEIN", "")))
                .andExpect(status().isConflict());

        // Validation conditionnelle
        mockMvc.perform(post("/api/evenements/THE-001/reservations")
                        .contentType(MediaType.APPLICATION_JSON).content(reserver("c@mail.ht", "GROUPE", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erreurs.nombrePersonnes").exists());

        // Annulation : 401 sans auth, 200 avec agent, puis promotion de la liste d'attente
        mockMvc.perform(delete(location)).andExpect(status().isUnauthorized());
        mockMvc.perform(delete(location).with(httpBasic("agent", "agent123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("ANNULEE"));
        mockMvc.perform(delete(location).with(httpBasic("agent", "agent123")))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/evenements/THE-001"))
                .andExpect(jsonPath("$.placesRestantes").value(0));

        // Liste paginee publique + parametres invalides
        mockMvc.perform(get("/api/evenements").param("categorie", "THEATRE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu[0].code").value("THE-001"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.taille").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/evenements").param("taille", "0")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/evenements").param("categorie", "OPERA")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reservations/abc")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/reservations/9999")).andExpect(status().isNotFound());
    }
}
