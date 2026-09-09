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
    private String validBody(int number, String status) {
        return """
                {
                  "number": %d,
                  "year": 2026,
                  "status": "%s",
                  "typeFolioId": 1,
                  "notaryId": 1
                }
                """.formatted(number, status);
    }

    @Test
    @DisplayName("Should create folio with status Nuevo")
    void shouldCreateFolioWithStatusNuevo() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9001, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(mapper.readTree(response).get("status").asText()).isEqualTo("Nuevo");
    }

    @Test
    @DisplayName("Should create folio with status Utilizado")
    void shouldCreateFolioWithStatusUtilizado() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9002, "Utilizado")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should create folio with status Errose")
    void shouldCreateFolioWithStatusErrose() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9003, "Errose")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 400 when tipoFolioId is missing")
    void shouldReturn400WhenTypeFolioIdMissing() throws Exception {
        String body = """
                {"number": 9004, "year": 2026, "status": "Nuevo", "notaryId": 1}
                """;
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when escribanoId is missing")
    void shouldReturn400WhenNotaryIdMissing() throws Exception {
        String body = """
                {"number": 9005, "year": 2026, "status": "Nuevo", "typeFolioId": 1}
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
    @DisplayName("Should update folio status from Nuevo to Utilizado")
    void shouldUpdateFolioStatus() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9007, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        String updateBody = """
                {"number": 9007, "year": 2026, "status": "Utilizado", "typeFolioId": 1, "notaryId": 1}
                """;
        MvcResult update = mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(mapper.readTree(update.getResponse().getContentAsString()).get("status").asText())
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

    @Test
    @DisplayName("CU63 — GET /search?status= should return only folios matching the status")
    void shouldSearchFoliosByStatus() throws Exception {
        mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9010, "Errose")))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/v1/folio/search").param("status", "Errose"))
                .andExpect(status().isOk())
                .andReturn();

        var nodes = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(nodes.isArray()).isTrue();
        for (var node : nodes) {
            assertThat(node.get("status").asText()).isEqualTo("Errose");
        }
    }

    @Test
    @DisplayName("GET /{id}/in-use should report false for a folio with status Nuevo")
    void shouldReportNotInUseForNuevoFolio() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9011, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(get("/api/v1/folio/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("GET /{id}/in-use should report true for a folio with status Utilizado")
    void shouldReportInUseForUtilizadoFolio() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9012, "Utilizado")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(get("/api/v1/folio/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("PUT should return 409 when folio is already Utilizado")
    void shouldReturn409WhenUpdatingUtilizadoFolio() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9013, "Utilizado")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9013, "Errose")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("DELETE should return 409 when folio is already Utilizado")
    void shouldReturn409WhenDeletingUtilizadoFolio() throws Exception {
        MvcResult create = mockMvc.perform(post("/api/v1/folio").contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9014, "Utilizado")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(delete("/api/v1/folio/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    private int createDeed() throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Deed de prueba para vinculación de folio",
                  "status": "Sin Firmar",
                  "dateDeedrecording": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idDeed").asInt();
    }

    private String bodyWithDeed(int number, String status, Integer deedId) {
        return """
                {
                  "number": %d,
                  "year": 2026,
                  "status": "%s",
                  "typeFolioId": 1,
                  "notaryId": 1,
                  "deedId": %s
                }
                """.formatted(number, status, deedId);
    }

    @Test
    @DisplayName("CU06/#838 — POST should link folio to deed and force status Utilizado")
    void shouldLinkFolioToDeedOnCreate() throws Exception {
        int idDeed = createDeed();

        MvcResult result = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9020, "Nuevo", idDeed)))
                .andExpect(status().isCreated())
                .andReturn();

        var json = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("status").asText()).isEqualTo("Utilizado");
        assertThat(json.get("deed").get("idDeed").asInt()).isEqualTo(idDeed);
    }

    @Test
    @DisplayName("CU06/#838 — PUT should link folio to deed and force status Utilizado")
    void shouldLinkFolioToDeedOnUpdate() throws Exception {
        int idDeed = createDeed();
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody(9021, "Nuevo")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        MvcResult update = mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9021, "Nuevo", idDeed)))
                .andExpect(status().isOk())
                .andReturn();

        var json = mapper.readTree(update.getResponse().getContentAsString());
        assertThat(json.get("status").asText()).isEqualTo("Utilizado");
        assertThat(json.get("deed").get("idDeed").asInt()).isEqualTo(idDeed);
    }

    @Test
    @DisplayName("CU06/#838 — POST should return 400 when escrituraId does not exist")
    void shouldReturn400WhenDeedIdNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9022, "Nuevo", 999999)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CU06/#838 — PUT should return 409 when folio is already Utilizado by another deed")
    void shouldRejectLinkingFolioAlreadyUtilizadoByAnotherDeed() throws Exception {
        int idDeedA = createDeed();
        int idDeedB = createDeed();
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9023, "Nuevo", idDeedA)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9023, "Nuevo", idDeedB)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("CU06/#838 — PUT should allow re-saving a folio already linked to the same deed")
    void shouldAllowReSavingFolioWithSameDeed() throws Exception {
        int idDeed = createDeed();
        MvcResult create = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9024, "Nuevo", idDeed)))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = mapper.readTree(create.getResponse().getContentAsString()).get("idFolio").asInt();

        mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyWithDeed(9024, "Utilizado", idDeed)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Utilizado"));
    }
}
