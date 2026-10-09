package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.licensis.notaire.repository.TestimonyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655 (CU07 / CU08): a testimony is a certified copy of a deed, so the bare
 * {@code POST /api/v1/testimonio} must not store one without its deed. (The UI creates
 * testimonies through {@code POST /testimonio/{idDeed}/generar}, which already takes the deed.)
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Bare testimony create requires its deed (issue #655)")
class TestimonyCreateRequiresDeedIntegrationTest {

    private static final String URL = "/api/v1/testimonio";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private TestimonyRepository testimonyRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private ResultActions postJson(String json) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    @Test
    @DisplayName("create without a deed is rejected with 400 naming it and stores nothing")
    void createWithoutDeedIsRejected() throws Exception {
        long before = testimonyRepository.count();

        postJson("{\"number\": 655, \"flagged\": false}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("deed")));

        assertThat(testimonyRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("create with a deed but no deed id is rejected with 400 and stores nothing")
    void createWithoutDeedIdIsRejected() throws Exception {
        long before = testimonyRepository.count();

        postJson("{\"number\": 655, \"deed\": {\"number\": 0}}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("idDeed")));

        assertThat(testimonyRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("create for an unknown deed answers 404 and stores nothing")
    void createForUnknownDeedIsNotFound() throws Exception {
        long before = testimonyRepository.count();

        postJson("{\"number\": 655, \"deed\": {\"idDeed\": 999999, \"number\": 0}}")
                .andExpect(status().isNotFound());

        assertThat(testimonyRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("the contract marks the deed required on create only")
    void contractMarksDeedRequiredOnCreate() throws Exception {
        JsonNode spec = JsonMapper.builder().build().readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertThat(requiredOf(spec, "post", URL)).contains("deed");
        assertThat(requiredOf(spec, "put", URL + "/{id}")).doesNotContain("deed");
    }

    private static List<String> requiredOf(JsonNode spec, String method, String path) {
        String ref = spec.path("paths").path(path).path(method).path("requestBody").path("content")
                .path("application/json").path("schema").path("$ref").asText();
        JsonNode schema = spec.path("components").path("schemas").path(ref.substring(ref.lastIndexOf('/') + 1));
        List<String> required = new ArrayList<>();
        schema.path("required").forEach(n -> required.add(n.asText()));
        return required;
    }
}
