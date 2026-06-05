package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("CU28/CU33/CU63 — Folio CRUD with valid estados and required fields")
class FolioControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /** Valid body with all required fields */
    private String validBody(int numero, String estado) {
        return """
                {
                  "numero": %d,
                  "anio": 2026,
                  "estado": "%s",
                  "tipoFolioId": 1,
                  "escribanoId": 1
                }
                """.formatted(numero, estado);
    }

    @Test
    @DisplayName("Should create folio with estado Nuevo")
    void shouldCreateFolioWithEstadoNuevo() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9001, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(mapper.readTree(response).get("estado").asText()).isEqualTo("Nuevo");
    }

    @Test
    @DisplayName("Should create folio with estado Utilizado")
    void shouldCreateFolioWithEstadoUtilizado() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9002, "Utilizado")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should create folio with estado Errose")
    void shouldCreateFolioWithEstadoErrose() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9003, "Errose")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 when tipoFolioId is missing")
    void shouldReturn400WhenTipoFolioIdMissing() throws Exception {
        String body = """
                {"numero": 9004, "anio": 2026, "estado": "Nuevo", "escribanoId": 1}
                """;
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when escribanoId is missing")
    void shouldReturn400WhenEscribanoIdMissing() throws Exception {
        String body = """
                {"numero": 9005, "anio": 2026, "estado": "Nuevo", "tipoFolioId": 1}
                """;
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should list all folios")
    void shouldListFolios() throws Exception {
        mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON).content(validBody(9006, "Nuevo")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/folio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should update folio estado from Nuevo to Utilizado")
    void shouldUpdateFolioEstado() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9007, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        String updateBody = """
                {"numero": 9007, "anio": 2026, "estado": "Utilizado", "tipoFolioId": 1, "escribanoId": 1}
                """;
        MvcResult update = mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mapper.readTree(update.getResponse().getContentAsString()).get("estado").asText())
                .isEqualTo("Utilizado");
    }

    @Test
    @DisplayName("Should delete folio and return 404 on subsequent GET")
    void shouldDeleteFolioAndReturnNotFound() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9008, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(delete("/api/v1/folio/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/folio/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent folio")
    void shouldReturn404WhenDeletingNonExistentFolio() throws Exception {
        mockMvc.perform(delete("/api/v1/folio/999999"))
                .andExpect(status().isNotFound());
    }
}
