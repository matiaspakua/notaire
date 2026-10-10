package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.licensis.notaire.business.Copy;
import com.licensis.notaire.repository.CopyRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655 (empty bodies answering 500): {@code POST /copia} without a print date broke the
 * NOT NULL {@code print_date} column, and {@code POST /minutas-inscripcion} and
 * {@code POST /gestiones/{id}/reingreso-documentacion} without their ids passed {@code null} to
 * {@code findById}; all answered 500. The required fields must be validated at the boundary
 * (400 naming each field, nothing stored) and marked required in the contract.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Copy, registration draft and document re-entry require their fields (issue #655)")
class RequiredRequestIdsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CopyRepository copyRepository;

    private MockMvc mockMvc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /copia with an empty body answers 400 naming number and datePrinting, and stores nothing")
    void copyCreateRequiresNumberAndPrintDate() throws Exception {
        long before = copyRepository.count();
        mockMvc.perform(post("/api/v1/copia").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", allOf(containsString("number"), containsString("datePrinting"))));
        assertThat(copyRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("PUT /copia/{id} without datePrinting answers 400, not 500")
    void copyUpdateRequiresPrintDate() throws Exception {
        Copy copy = new Copy();
        copy.setNumber(655);
        copy.setDatePrinting(new Date());
        int id = copyRepository.save(copy).getIdCopy();

        mockMvc.perform(put("/api/v1/copia/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"number\":655}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("datePrinting")));
    }

    @Test
    @DisplayName("POST /minutas-inscripcion with an empty body answers 400 naming idDeed, not 500")
    void registrationDraftRequiresDeed() throws Exception {
        mockMvc.perform(post("/api/v1/minutas-inscripcion").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("idDeed")));
    }

    @Test
    @DisplayName("POST /gestiones/{id}/reingreso-documentacion with an empty body answers 400 naming both ids, not 500")
    void reentryRequiresProcedureAndDocumentType() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/1/reingreso-documentacion")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message",
                        allOf(containsString("idProcedure"), containsString("idDocumentType"))));
    }

    @ParameterizedTest(name = "contract: {0} {1} requires {2}")
    @CsvSource({
        "/api/v1/copia, post, number;datePrinting",
        "/api/v1/copia/{id}, put, number;datePrinting",
        "/api/v1/minutas-inscripcion, post, idDeed",
        "/api/v1/gestiones/{id}/reingreso-documentacion, post, idProcedure;idDocumentType"
    })
    @DisplayName("the contract marks the fields required")
    void contractMarksFieldsRequired(String path, String method, String fields) throws Exception {
        JsonNode spec = mapper.readTree(mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String ref = spec.path("paths").path(path).path(method).path("requestBody").path("content")
                .path("application/json").path("schema").path("$ref").asText();
        JsonNode schema = spec.path("components").path("schemas").path(ref.substring(ref.lastIndexOf('/') + 1));
        List<String> required = new ArrayList<>();
        schema.path("required").forEach(n -> required.add(n.asText()));

        assertThat(required).contains(fields.split(";"));
    }
}
