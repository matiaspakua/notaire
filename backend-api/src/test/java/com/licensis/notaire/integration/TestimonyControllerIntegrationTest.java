package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU07", "CU08", "CU12", "CU44"})
@DisplayName("TestimonioController — CU07/CU08 CRUD integration tests")
class TestimonyControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createDeed() throws Exception {
        String deedBody = """
                {
                  "number": 3001,
                  "body": "Deed de prueba para testimony",
                  "status": "BORRADOR",
                  "dateDeedrecording": "2026-06-16"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deedBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idDeed").asInt();
    }

    private int createDeedFirmada() throws Exception {
        String deedBody = """
                {
                  "number": 3002,
                  "body": "Deed firmada de prueba para generar testimony",
                  "status": "Firmada",
                  "dateDeedrecording": "2026-06-16"
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deedBody))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("idDeed").asInt();
    }

    // DtoEscritura.numero is primitive int — must be included in nested object to avoid 400
    private String testimonyBody(int number, boolean flagged, int deedId) {
        return """
                {
                  "number": %d,
                  "flagged": %s,
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(number, flagged, deedId);
    }

    @Test
    @DisplayName("CU07 - Should return 200 and empty array when no testimonios exist")
    void shouldReturnEmptyListWhenNoTestimonios() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("CU07 - Should return 404 for non-existing testimony")
    void shouldReturn404ForNonExistingTestimony() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should create testimony linked to deed and return 201")
    void shouldCreateTestimonyLinkedToDeed() throws Exception {
        int deedId = createDeed();

        String body = """
                {
                  "number": 1001,
                  "flagged": false,
                  "notes": "Testimony de prueba",
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(deedId);

        MvcResult result = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTestimony").isNumber())
                .andExpect(jsonPath("$.number").value(1001))
                .andExpect(jsonPath("$.flagged").value(false))
                .andReturn();

        JsonNode response = mapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("idTestimony").asInt()).isPositive();
    }

    @Test
    @DisplayName("CU07 - Should retrieve created testimony by ID")
    void shouldRetrieveTestimonyById() throws Exception {
        int deedId = createDeed();

        String body = """
                {
                  "number": 1002,
                  "flagged": false,
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(deedId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        int id = mapper.readTree(created.getResponse().getContentAsString()).get("idTestimony").asInt();

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTestimony").value(id))
                .andExpect(jsonPath("$.number").value(1002));
    }

    @Test
    @DisplayName("CU08 - Should verify (mark as flagged) an existing testimony")
    void shouldMarkTestimonyAsFlagged() throws Exception {
        int deedId = createDeed();

        String createBody = """
                {
                  "number": 1003,
                  "flagged": false,
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(deedId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createdNode = mapper.readTree(created.getResponse().getContentAsString());
        int id = createdNode.get("idTestimony").asInt();
        int version = createdNode.get("version").asInt();

        String updateBody = """
                {
                  "idTestimony": %d,
                  "number": 1003,
                  "flagged": true,
                  "notes": "Verified por notary",
                  "version": %d,
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(id, version, deedId);

        mockMvc.perform(put("/api/v1/testimonio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flagged").value(true))
                .andExpect(jsonPath("$.notes").value("Verified por notary"));
    }

    @Test
    @DisplayName("CU12 - Should delete an existing testimony")
    void shouldDeleteExistingTestimony() throws Exception {
        int deedId = createDeed();

        String body = """
                {
                  "number": 1004,
                  "flagged": false,
                  "deed": {"idDeed": %d, "number": 0}
                }
                """.formatted(deedId);

        MvcResult created = mockMvc.perform(post("/api/v1/testimonio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        int id = mapper.readTree(created.getResponse().getContentAsString()).get("idTestimony").asInt();

        mockMvc.perform(delete("/api/v1/testimonio/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/testimonio/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should return 404 when updating non-existing testimony")
    void shouldReturn404WhenUpdatingNonExistingTestimony() throws Exception {
        String body = """
                {"number": 9999, "flagged": false}
                """;
        mockMvc.perform(put("/api/v1/testimonio/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should return 404 when deleting non-existing testimony")
    void shouldReturn404WhenDeletingNonExistingTestimony() throws Exception {
        mockMvc.perform(delete("/api/v1/testimonio/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU07 - Should generate testimony with system-assigned number from a Firmada deed")
    void shouldGenerateTestimonyFromFirmadaDeed() throws Exception {
        int deedId = createDeedFirmada();

        mockMvc.perform(post("/api/v1/testimonio/" + deedId + "/generar"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTestimony").isNumber())
                .andExpect(jsonPath("$.number").isNumber())
                .andExpect(jsonPath("$.verified").value(false))
                .andExpect(jsonPath("$.flagged").value(false))
                .andExpect(jsonPath("$.deed.idDeed").value(deedId));
    }

    @Test
    @DisplayName("CU07 - Should return 400 when generating testimony from a non-Firmada deed")
    void shouldRejectGenerateTestimonyFromNonFirmadaDeed() throws Exception {
        int deedId = createDeed();

        mockMvc.perform(post("/api/v1/testimonio/" + deedId + "/generar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CU08 - Should verify a generated testimony without notes")
    void shouldVerifyGeneratedTestimonyWithoutNotes() throws Exception {
        int deedId = createDeedFirmada();

        MvcResult generated = mockMvc.perform(post("/api/v1/testimonio/" + deedId + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        int idTestimony = mapper.readTree(generated.getResponse().getContentAsString())
                .get("idTestimony").asInt();

        mockMvc.perform(post("/api/v1/testimonio/" + idTestimony + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flagged\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.flagged").value(false));
    }

    @Test
    @DisplayName("CU08 - Should verify a generated testimony with notes")
    void shouldVerifyGeneratedTestimonyWithNotes() throws Exception {
        int deedId = createDeedFirmada();

        MvcResult generated = mockMvc.perform(post("/api/v1/testimonio/" + deedId + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        int idTestimony = mapper.readTree(generated.getResponse().getContentAsString())
                .get("idTestimony").asInt();

        mockMvc.perform(post("/api/v1/testimonio/" + idTestimony + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flagged\": true, \"notes\": \"Falta firma del otorgante\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true))
                .andExpect(jsonPath("$.flagged").value(true))
                .andExpect(jsonPath("$.notes").value("Falta firma del otorgante"));
    }
}
