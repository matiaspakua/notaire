package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655, Owner decision (Oct 9): a missing or stale {@code version} on an update must not
 * answer 500. {@code PUT /testimonio/{id}} requires {@code version} (400 when absent) and answers
 * {@code 409 Conflict} when it is not the stored one (optimistic lock). Every update whose
 * failure goes through {@code ErrorResponses.updateFailed} or {@code GlobalExceptionHandler}
 * answers 409 for an optimistic-lock failure, so the catalog PUTs with a stale version get 409 too.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Missing or stale version on update answers 400 / 409, not 500 (issue #655)")
class StaleOrMissingVersionIntegrationTest {

    private static final String STALE_MESSAGE =
            "The record was modified by another user; reload it and try again";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private JsonNode json(String body) throws Exception {
        return mapper.readTree(body);
    }

    private JsonNode getJson(String url) throws Exception {
        return json(mockMvc.perform(get(url)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private int createTestimony() throws Exception {
        return json(mockMvc.perform(post("/api/v1/testimonio").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":71,\"flagged\":true,\"verified\":true,\"notes\":\"keep\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString())
                .get("idTestimony").asInt();
    }

    private void putTestimony(int id, String body, int expected) throws Exception {
        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(expected));
    }

    @Test
    @DisplayName("PUT /testimonio/{id} without version answers 400 naming version and stores nothing")
    void testimonyMissingVersionIsRejected() throws Exception {
        int id = createTestimony();

        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":72,\"flagged\":false,\"verified\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("version: es obligatorio"));

        assertThat(getJson("/api/v1/testimonio/" + id).get("number").asInt()).isEqualTo(71);
    }

    @Test
    @DisplayName("PUT /testimonio/{id} with a stale version answers 409 and keeps the newer data")
    void testimonyStaleVersionIsConflict() throws Exception {
        int id = createTestimony();
        int version = getJson("/api/v1/testimonio/" + id).get("version").asInt();

        putTestimony(id, "{\"number\":73,\"flagged\":true,\"verified\":true,\"version\":" + version + "}", 200);
        assertThat(getJson("/api/v1/testimonio/" + id).get("version").asInt()).isEqualTo(version + 1);

        mockMvc.perform(put("/api/v1/testimonio/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"number\":74,\"flagged\":false,\"verified\":false,\"version\":" + version + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(STALE_MESSAGE));

        JsonNode stored = getJson("/api/v1/testimonio/" + id);
        assertThat(stored.get("number").asInt()).isEqualTo(73);
        assertThat(stored.get("version").asInt()).isEqualTo(version + 1);
    }

    @Test
    @DisplayName("PUT /testimonio/{id} with a version ahead of the stored one also answers 409")
    void testimonyFutureVersionIsConflict() throws Exception {
        int id = createTestimony();
        int version = getJson("/api/v1/testimonio/" + id).get("version").asInt();

        putTestimony(id, "{\"number\":75,\"flagged\":true,\"verified\":true,\"version\":" + (version + 5) + "}", 409);
        assertThat(getJson("/api/v1/testimonio/" + id).get("number").asInt()).isEqualTo(71);
    }

    @Test
    @DisplayName("a catalog PUT with a stale version answers 409 instead of 500")
    void catalogStaleVersionIsConflict() throws Exception {
        JsonNode created = json(mockMvc.perform(post("/api/v1/tipo-folio").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TF655-" + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        int id = created.get("idFolioType").asInt();
        JsonNode current = getJson("/api/v1/tipo-folio/" + id);
        String stale = current.toString().replaceFirst("\"version\":\\d+",
                "\"version\":" + (current.get("version").asInt() + 5));

        mockMvc.perform(put("/api/v1/tipo-folio/" + id).contentType(MediaType.APPLICATION_JSON).content(stale))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("reload it")));
    }

    @Test
    @DisplayName("the contract of PUT /testimonio/{id} requires version and documents 409")
    void contractRequiresVersionAndDocuments409() throws Exception {
        JsonNode spec = getJson("/v3/api-docs");
        JsonNode operation = spec.path("paths").path("/api/v1/testimonio/{id}").path("put");

        List<String> codes = new ArrayList<>();
        operation.path("responses").fieldNames().forEachRemaining(codes::add);
        assertThat(codes).contains("400", "409");

        String ref = operation.path("requestBody").path("content").path("application/json").path("schema")
                .path("$ref").asText().replace("#/components/schemas/", "");
        List<String> required = new ArrayList<>();
        spec.path("components").path("schemas").path(ref).path("required").forEach(n -> required.add(n.asText()));
        assertThat(required).contains("number", "flagged", "verified", "version");
    }
}
