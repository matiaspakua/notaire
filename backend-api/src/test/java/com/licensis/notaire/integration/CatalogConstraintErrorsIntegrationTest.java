package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #579 (slice 2, catalog controllers; Owner decision Run 7): a create or update whose
 * data violates a database constraint is a client error. The catalog controllers caught it
 * themselves and answered 409 on create and 500 on update, while {@code GlobalExceptionHandler}
 * answers 400 for the same {@code DataIntegrityViolationException}. They must answer 400 with
 * the standard {@code ErrorResponse} and the same safe message, and the contract must document
 * 400 on both.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Catalog create/update constraint errors answer 400 (issue #579)")
class CatalogConstraintErrorsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    /** Same text as {@code GlobalExceptionHandler} for a {@code DataIntegrityViolationException}. */
    private static final String CONSTRAINT_MESSAGE = "The submitted data violates a database constraint";

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @ParameterizedTest(name = "POST {0} with an empty body answers 400")
    @CsvSource({"/api/v1/conceptos", "/api/v1/estado-gestion", "/api/v1/tipo-de-documento",
        "/api/v1/tipo-folio", "/api/v1/tipo-tramite"})
    @DisplayName("a create that violates a NOT NULL constraint answers 400, not 409")
    void createConstraintViolationIsBadRequest(String url) throws Exception {
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(CONSTRAINT_MESSAGE));
    }

    @ParameterizedTest(name = "PUT {0}/'{id}' without a name answers 400")
    @CsvSource({
        "/api/v1/estado-gestion, idManagementStatus, '{\"name\":\"E579\"}'",
        "/api/v1/tipo-de-documento, idDocumentType, '{\"name\":\"D579\"}'",
        "/api/v1/tipo-folio, idFolioType, '{\"name\":\"F579\"}'",
        "/api/v1/tipo-tramite, idProcedureType, '{\"name\":\"T579\"}'"
    })
    @DisplayName("an update that violates a NOT NULL constraint answers 400, not 500")
    // Concept updates are partial (Concept.setAtributos skips null fields), so a null name
    // keeps the stored one; the concept update path is covered by ErrorResponsesTest.
    void updateConstraintViolationIsBadRequest(String url, String idField, String createBody) throws Exception {
        JsonNode created = mapper.readTree(mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON)
                        .content(createBody.replace("579", "579-" + System.nanoTime())))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        int id = created.get(idField).asInt();
        ObjectNode current = (ObjectNode) mapper.readTree(mockMvc.perform(get(url + "/" + id))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        current.putNull("name");

        mockMvc.perform(put(url + "/" + id).contentType(MediaType.APPLICATION_JSON).content(current.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(CONSTRAINT_MESSAGE));
    }

    @ParameterizedTest(name = "contract of {0} documents 400 on POST and PUT")
    @CsvSource({"/api/v1/conceptos", "/api/v1/estado-gestion", "/api/v1/tipo-de-documento",
        "/api/v1/tipo-folio", "/api/v1/tipo-tramite"})
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
