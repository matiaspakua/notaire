package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.licensis.notaire.business.Property;
import com.licensis.notaire.repository.PropertyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * Issue #655 (CU69 / CU82): a property is identified by its cadastral designation
 * (nomenclatura catastral). The UI already labels it required; the API must reject a
 * create or full update without it instead of storing an unidentifiable property.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Property requests require the cadastral designation (issue #655)")
class PropertyRequestValidationIntegrationTest {

    private static final String URL = "/api/v1/inmueble";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private PropertyRepository propertyRepository;

    private MockMvc mockMvc;
    private Property stored;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        stored = new Property();
        stored.setCadastralDesignation("NC-655-STORED");
        stored.setAddress("Calle #655");
        stored = propertyRepository.save(stored);
    }

    @AfterEach
    void tearDown() {
        propertyRepository.findById(stored.getIdProperty()).ifPresent(propertyRepository::delete);
    }

    @ParameterizedTest(name = "create with body {0} is rejected")
    @ValueSource(strings = {
        "{}",
        "{\"address\": \"Sin nomenclatura\"}",
        "{\"cadastralDesignation\": null, \"address\": \"Nula\"}",
        "{\"cadastralDesignation\": \"\", \"address\": \"Vacia\"}",
        "{\"cadastralDesignation\": \"   \", \"address\": \"Blanca\"}"
    })
    @DisplayName("create without a cadastral designation is rejected and stores nothing")
    void createRequiresCadastralDesignation(String body) throws Exception {
        long before = propertyRepository.count();

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("cadastralDesignation")));

        assertThat(propertyRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("a full update without a cadastral designation is rejected and keeps the stored one")
    void updateRequiresCadastralDesignation() throws Exception {
        mockMvc.perform(put(URL + "/" + stored.getIdProperty()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"address\": \"Otra calle\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("cadastralDesignation")));

        Property reloaded = propertyRepository.findById(stored.getIdProperty()).orElseThrow();
        assertThat(reloaded.getCadastralDesignation()).isEqualTo("NC-655-STORED");
        assertThat(reloaded.getAddress()).isEqualTo("Calle #655");
    }

    @Test
    @DisplayName("the contract marks cadastralDesignation required on create and update")
    void contractMarksCadastralDesignationRequired() throws Exception {
        JsonNode spec = JsonMapper.builder().build().readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertThat(requiredOf(spec, "post", URL)).contains("cadastralDesignation");
        assertThat(requiredOf(spec, "put", URL + "/{id}")).contains("cadastralDesignation");
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
