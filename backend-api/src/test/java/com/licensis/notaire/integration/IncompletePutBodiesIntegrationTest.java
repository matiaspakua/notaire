package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.licensis.notaire.business.Deed;
import com.licensis.notaire.repository.DeedRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655, Owner decision (Oct 9): an empty or incomplete PUT body answers 400 with the
 * standard {@code ErrorResponse} naming every missing field, instead of 200. "Incomplete" means a
 * required field of the entity is missing: the POST rule or the NOT NULL column it maps to.
 * <ul>
 *   <li>{@code PUT /testimonio/{id}}: {@code number}, {@code flagged}, {@code verified} (NOT NULL
 *       columns; {@code {}} used to overwrite them with 0/false).</li>
 *   <li>{@code PUT /tramites/{id}}: {@code idProcedureType} (required on POST).</li>
 *   <li>{@code PUT /tipo-tramite/{id}/workflow}: {@code workflowDefinitionId} must be present;
 *       an explicit {@code null} unassigns ({@code {}} used to unassign silently).</li>
 * </ul>
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Empty or incomplete PUT bodies answer 400 (issue #655)")
class IncompletePutBodiesIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Autowired
    private DeedRepository deedRepository;

    /** POST /testimonio requires an existing deed (#1335), so each fixture testimony copies one. */
    private int seedDeed() {
        Deed deed = new Deed();
        deed.setNumber((int) (System.nanoTime() % 1_000_000));
        deed.setDateDeedrecording(new Date());
        deed.setStatus("Firmada");
        return deedRepository.save(deed).getIdDeed();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private JsonNode postJson(String url, String body) throws Exception {
        return mapper.readTree(mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode getJson(String url) throws Exception {
        return mapper.readTree(mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private int createProcedureType() throws Exception {
        return postJson("/api/v1/tipo-tramite", "{\"name\":\"T655-" + System.nanoTime() + "\"}")
                .get("idProcedureType").asInt();
    }

    // ---------------------------------------------------------------- PUT /testimonio/{id}

    private int createTestimony() throws Exception {
        return postJson("/api/v1/testimonio",
                "{\"number\":57,\"flagged\":true,\"verified\":true,\"notes\":\"keep\",\"deed\":{\"idDeed\":"
                        + seedDeed() + ",\"number\":0}}").get("idTestimony").asInt();
    }

    @Test
    @DisplayName("PUT /testimonio/{id} with {} answers 400 naming number, flagged and verified, and stores nothing")
    void testimonyEmptyBodyIsRejected() throws Exception {
        int id = createTestimony();

        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(allOf(containsString("number: es obligatorio"),
                        containsString("flagged: es obligatorio"), containsString("verified: es obligatorio"))));

        JsonNode stored = getJson("/api/v1/testimonio/" + id);
        assertThat(stored.get("number").asInt()).isEqualTo(57);
        assertThat(stored.get("flagged").asBoolean()).isTrue();
        assertThat(stored.get("verified").asBoolean()).isTrue();
        assertThat(stored.get("notes").asText()).isEqualTo("keep");
    }

    @Test
    @DisplayName("PUT /testimonio/{id} with only some required fields answers 400 naming the missing ones")
    void testimonyIncompleteBodyIsRejected() throws Exception {
        int id = createTestimony();

        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":58,\"flagged\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(allOf(containsString("verified: es obligatorio"),
                        not(containsString("number")), not(containsString("flagged")))));

        assertThat(getJson("/api/v1/testimonio/" + id).get("number").asInt()).isEqualTo(57);
    }

    @Test
    @DisplayName("PUT /testimonio/{id} with a complete body still answers 200 and stores it")
    void testimonyCompleteBodyIsStored() throws Exception {
        int id = createTestimony();

        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":59,\"flagged\":false,\"verified\":false,\"notes\":\"n\",\"version\":0}"))
                .andExpect(status().isOk());

        JsonNode stored = getJson("/api/v1/testimonio/" + id);
        assertThat(stored.get("number").asInt()).isEqualTo(59);
        assertThat(stored.get("flagged").asBoolean()).isFalse();
        assertThat(stored.get("verified").asBoolean()).isFalse();
    }

    // ---------------------------------------------------------------- PUT /tramites/{id}

    @Test
    @DisplayName("PUT /tramites/{id} without idProcedureType answers 400 and keeps the stored procedure")
    void procedureWithoutTypeIsRejected() throws Exception {
        int type = createProcedureType();
        int id = postJson("/api/v1/tramites", "{\"idProcedureType\":" + type + ",\"notes\":\"keep\"}")
                .get("idProcedure").asInt();

        for (String body : List.of("{}", "{\"notes\":\"x\"}")) {
            mockMvc.perform(put("/api/v1/tramites/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("idProcedureType: es obligatorio"));
        }
        assertThat(getJson("/api/v1/tramites/" + id).get("notes").asText()).isEqualTo("keep");

        mockMvc.perform(put("/api/v1/tramites/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idProcedureType\":" + type + ",\"notes\":\"new\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value("new"));
    }

    @Test
    @DisplayName("POST /tramites without idProcedureType answers 400 with the same ErrorResponse")
    void procedureCreateWithoutTypeUsesErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/tramites").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("idProcedureType: es obligatorio"));
    }

    // ---------------------------------------------------------------- PUT /tipo-tramite/{id}/workflow

    @Test
    @DisplayName("PUT /tipo-tramite/{id}/workflow with {} answers 400; an explicit null still unassigns")
    void workflowAssignmentRequiresTheField() throws Exception {
        int type = createProcedureType();

        mockMvc.perform(put("/api/v1/tipo-tramite/" + type + "/workflow").contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("workflowDefinitionId: es obligatorio")));

        mockMvc.perform(put("/api/v1/tipo-tramite/" + type + "/workflow").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowDefinitionId\":null}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /tipo-tramite/{id}/workflow with a non-integer id answers 400, not 500")
    void workflowAssignmentRejectsNonIntegerId() throws Exception {
        int type = createProcedureType();

        mockMvc.perform(put("/api/v1/tipo-tramite/" + type + "/workflow").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"workflowDefinitionId\":\"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------------------------------------------------------------- contract

    @ParameterizedTest(name = "contract of PUT {0} documents 400 and requires {1}")
    @CsvSource({
        "/api/v1/testimonio/{id}, number;flagged;verified",
        "/api/v1/tramites/{id}, idProcedureType",
        "/api/v1/tipo-tramite/{id}/workflow, workflowDefinitionId"
    })
    @DisplayName("the contract documents 400 and the required request fields")
    void contractDocumentsRequiredFields(String path, String requiredFields) throws Exception {
        JsonNode spec = getJson("/v3/api-docs");
        JsonNode operation = spec.path("paths").path(path).path("put");

        List<String> codes = new ArrayList<>();
        operation.path("responses").fieldNames().forEachRemaining(codes::add);
        assertThat(codes).contains("400");

        JsonNode schema = operation.path("requestBody").path("content").path("application/json").path("schema");
        if (schema.has("$ref")) {
            String name = schema.get("$ref").asText().replace("#/components/schemas/", "");
            schema = spec.path("components").path("schemas").path(name);
        }
        List<String> required = new ArrayList<>();
        schema.path("required").forEach(node -> required.add(node.asText()));
        assertThat(required).contains(requiredFields.split(";"));
    }
}
