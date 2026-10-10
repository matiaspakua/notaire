package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #579 (slice 4): against the real schema, an empty testimony movement violates the NOT
 * NULL entry date and answers 400 instead of 409, and the contract documents 400 on the create
 * and update of testimony movements, testimonies and people.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Testimony and person constraint errors answer 400 (issue #579)")
class TestimonyPersonConstraintErrorsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /movimiento-testimonio with an empty body answers 400, not 409")
    void emptyMovementIsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/movimiento-testimonio").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The submitted data violates a database constraint"));
    }

    @ParameterizedTest(name = "contract of {0} documents 400 on POST and PUT")
    @CsvSource({"/api/v1/movimiento-testimonio", "/api/v1/testimonio", "/api/v1/people"})
    @DisplayName("the contract documents 400 on create and update")
    void contractDocumentsBadRequest(String url) throws Exception {
        JsonNode spec = mapper.readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertThat(codes(spec, url, "post")).contains("400");
        assertThat(codes(spec, url + "/{id}", "put")).contains("400");
    }

    private static List<String> codes(JsonNode spec, String path, String method) {
        List<String> codes = new ArrayList<>();
        spec.path("paths").path(path).path(method).path("responses").fieldNames().forEachRemaining(codes::add);
        return codes;
    }
}
