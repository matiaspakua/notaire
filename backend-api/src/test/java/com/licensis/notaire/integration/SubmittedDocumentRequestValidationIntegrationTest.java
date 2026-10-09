package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #655 (CU04 / CU72): {@code /api/v1/documento-presentado} must reject what it cannot
 * store instead of silently dropping it. Rows are committed and reloaded (no test
 * transaction), as in two real HTTP requests.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Submitted document requests are validated at the boundary (issue #655)")
class SubmittedDocumentRequestValidationIntegrationTest {

    private static final String URL = "/api/v1/documento-presentado";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private SubmittedDocumentRepository documentRepository;
    @Autowired
    private DocumentTypeRepository documentTypeRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
    @Autowired
    private ProcedureRepository procedureRepository;

    private MockMvc mockMvc;
    private DocumentType expiringType;
    private ProcedureType procedureType;
    private Procedure procedure;
    private SubmittedDocument stored;
    private final List<Integer> createdDocuments = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        expiringType = new DocumentType();
        expiringType.setName("Tipo #655");
        expiringType.setExpires(true);
        expiringType.setDueDays(10);
        expiringType.setDeliveredBy("Cliente");
        expiringType.setEnabled(true);
        expiringType = documentTypeRepository.save(expiringType);

        procedureType = new ProcedureType();
        procedureType.setName("Tipo trámite #655");
        procedureType.setEnabled(true);
        procedureType = procedureTypeRepository.save(procedureType);
        procedure = new Procedure();
        procedure.setFkIdProcedureType(procedureType);
        procedure = procedureRepository.save(procedure);

        stored = new SubmittedDocument();
        stored.setDocumentType(expiringType);
        stored.setName("Documento #655");
        stored.setDelivered(false);
        stored.setPrepared(false);
        stored.setReleased(false);
        stored.setFlagged(false);
        stored.setReentered(false);
        stored.setFkIdProcedure(procedure);
        stored.setDateEntry(toDate(LocalDate.of(2026, 1, 1)));
        stored.setDeliveredBy("Cliente");
        stored = documentRepository.save(stored);
    }

    @AfterEach
    void tearDown() {
        createdDocuments.forEach(id -> documentRepository.findById(id).ifPresent(documentRepository::delete));
        documentRepository.findById(stored.getIdSubmittedDocument()).ifPresent(documentRepository::delete);
        procedureRepository.deleteById(procedure.getIdProcedure());
        procedureTypeRepository.deleteById(procedureType.getIdProcedureType());
        documentTypeRepository.deleteById(expiringType.getIdDocumentType());
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        return java.time.Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private ResultActions postJson(String json) throws Exception {
        return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions putJson(String json) throws Exception {
        return mockMvc.perform(put(URL + "/" + stored.getIdSubmittedDocument())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private SubmittedDocument reload() {
        return documentRepository.findById(stored.getIdSubmittedDocument()).orElseThrow();
    }

    @ParameterizedTest(name = "date \"{0}\" is rejected on create")
    @ValueSource(strings = {"05/09/2026", "2026-9-5", "2026-02-30", "not-a-date", ""})
    @DisplayName("create rejects a date that is not a real yyyy-MM-dd day")
    void createRejectsInvalidDate(String date) throws Exception {
        long before = documentRepository.count();

        postJson("{\"typeId\": " + expiringType.getIdDocumentType() + ", \"procedureId\": "
                + procedure.getIdProcedure() + ", \"date\": \"" + date + "\", \"name\": \"x\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("yyyy-MM-dd")));

        assertThat(documentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("create rejects an unknown document type instead of storing a document without type")
    void createRejectsUnknownType() throws Exception {
        long before = documentRepository.count();

        postJson("{\"typeId\": 999999, \"procedureId\": " + procedure.getIdProcedure()
                + ", \"date\": \"2026-09-05\", \"name\": \"x\"}")
                .andExpect(status().isNotFound());

        assertThat(documentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("create rejects an unknown procedure instead of storing an orphan document")
    void createRejectsUnknownProcedure() throws Exception {
        long before = documentRepository.count();

        postJson("{\"typeId\": " + expiringType.getIdDocumentType() + ", \"procedureId\": 999999, \"name\": \"x\"}")
                .andExpect(status().isNotFound());

        assertThat(documentRepository.count()).isEqualTo(before);
    }

    @ParameterizedTest(name = "create without {0} is rejected")
    @ValueSource(strings = {"typeId", "procedureId"})
    @DisplayName("create requires the document type and the procedure (CU04: a document is presented for a "
            + "procedure of a management)")
    void createRequiresTypeAndProcedure(String missing) throws Exception {
        long before = documentRepository.count();
        String type = "\"typeId\": " + expiringType.getIdDocumentType() + ", ";
        String proc = "\"procedureId\": " + procedure.getIdProcedure() + ", ";
        String body = "{" + ("typeId".equals(missing) ? "" : type) + ("procedureId".equals(missing) ? "" : proc)
                + "\"date\": \"2026-09-05\", \"name\": \"x\"}";

        postJson(body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(missing)));

        assertThat(documentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("an empty create body is rejected instead of storing an empty document")
    void createRejectsEmptyBody() throws Exception {
        long before = documentRepository.count();

        postJson("{}").andExpect(status().isBadRequest());

        assertThat(documentRepository.count()).isEqualTo(before);
    }

    @Test
    @DisplayName("the contract marks typeId and procedureId required on create only")
    void contractMarksCreateFieldsRequired() throws Exception {
        com.fasterxml.jackson.databind.JsonNode spec = JsonMapper.builder().build().readTree(
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(requiredOf(spec, "post", URL)).contains("typeId", "procedureId");
        assertThat(requiredOf(spec, "put", URL + "/{id}")).doesNotContain("typeId", "procedureId");
    }

    private static List<String> requiredOf(com.fasterxml.jackson.databind.JsonNode spec, String method, String path) {
        String ref = spec.path("paths").path(path).path(method).path("requestBody").path("content")
                .path("application/json").path("schema").path("$ref").asText();
        com.fasterxml.jackson.databind.JsonNode schema = spec.path("components").path("schemas")
                .path(ref.substring(ref.lastIndexOf('/') + 1));
        List<String> required = new ArrayList<>();
        schema.path("required").forEach(n -> required.add(n.asText()));
        return required;
    }

    @Test
    @DisplayName("a valid create still stores the date and computes the due date")
    void validCreateStillWorks() throws Exception {
        String body = postJson("{\"typeId\": " + expiringType.getIdDocumentType()
                        + ", \"date\": \"2026-09-05\", \"procedureId\": " + procedure.getIdProcedure()
                        + ", \"name\": \"ok\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.date").value("2026-09-05"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonMapper.builder().build().readTree(body).get("idSubmittedDocument").asInt();
        createdDocuments.add(id);

        SubmittedDocument created = documentRepository.findById(id).orElseThrow();
        assertThat(toLocalDate(created.getDateDue())).isEqualTo(LocalDate.of(2026, 9, 15));
    }

    @Test
    @DisplayName("update rejects an invalid date and keeps the stored one")
    void updateRejectsInvalidDate() throws Exception {
        putJson("{\"date\": \"2026-13-01\"}").andExpect(status().isBadRequest());

        assertThat(toLocalDate(reload().getDateEntry())).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("update rejects an unknown procedure and keeps the stored link")
    void updateRejectsUnknownProcedure() throws Exception {
        putJson("{\"procedureId\": 999999}").andExpect(status().isNotFound());

        assertThat(reload().getFkIdProcedure().getIdProcedure()).isEqualTo(procedure.getIdProcedure());
    }

    @Test
    @DisplayName("update rejects an unknown document type and keeps the stored one")
    void updateRejectsUnknownType() throws Exception {
        putJson("{\"typeId\": 999999}").andExpect(status().isNotFound());

        assertThat(reload().getDocumentType().getIdDocumentType()).isEqualTo(expiringType.getIdDocumentType());
    }

    @Test
    @DisplayName("update of a stored dated document recomputes the due date from the stored entry date")
    void updateRecomputesDueFromStoredDate() throws Exception {
        putJson("{\"deliveredBy\": \"Entidad Externa\"}").andExpect(status().isOk());

        SubmittedDocument after = reload();
        assertThat(after.getDeliveredBy()).isEqualTo("Entidad Externa");
        assertThat(toLocalDate(after.getDateDue())).isEqualTo(LocalDate.of(2026, 1, 11));
    }
}
