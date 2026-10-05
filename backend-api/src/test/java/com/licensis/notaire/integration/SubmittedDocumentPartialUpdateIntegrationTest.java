package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.SubmittedDocument;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("CU72 — updating a submitted document keeps what the request does not carry (#1241)")
class SubmittedDocumentPartialUpdateIntegrationTest {

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
    private Procedure procedure;
    private SubmittedDocument stored;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        expiringType = new DocumentType();
        expiringType.setName("Tipo con vencimiento");
        expiringType.setExpires(true);
        expiringType.setDueDays(10);
        expiringType.setDeliveredBy("Cliente");
        expiringType.setEnabled(true);
        expiringType = documentTypeRepository.save(expiringType);

        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Tipo trámite doc");
        procedureType.setEnabled(true);
        procedureType = procedureTypeRepository.save(procedureType);
        procedure = new Procedure();
        procedure.setFkIdProcedureType(procedureType);
        procedure = procedureRepository.save(procedure);

        stored = new SubmittedDocument();
        stored.setDocumentType(expiringType);
        stored.setName("Escritura original");
        stored.setNotes("nota previa");
        stored.setDelivered(false);
        stored.setPrepared(true);
        stored.setReleased(true);
        stored.setFlagged(true);
        stored.setReentered(true);
        stored.setFkIdProcedure(procedure);
        stored.setDateEntry(new SimpleDateFormat("yyyy-MM-dd").parse("2026-01-01"));
        stored.setDeliveredBy("Cliente");
        stored = documentRepository.saveAndFlush(stored);
    }

    private void putJson(Integer id, String json, int expectedStatus) throws Exception {
        mockMvc.perform(put("/api/v1/documento-presentado/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is(expectedStatus));
        documentRepository.flush();
    }

    private SubmittedDocument reload() {
        return documentRepository.findById(stored.getIdSubmittedDocument()).orElseThrow();
    }

    @Test
    @DisplayName("shouldKeepNameFlagsAndProcedureWhenOnlyDeliveredIsSent")
    void shouldKeepNameFlagsAndProcedureWhenOnlyDeliveredIsSent() throws Exception {
        putJson(stored.getIdSubmittedDocument(), "{\"delivered\":true}", 200);

        SubmittedDocument after = reload();
        assertThat(after.getDelivered()).isTrue();
        assertThat(after.getName()).isEqualTo("Escritura original");
        assertThat(after.getNotes()).isEqualTo("nota previa");
        assertThat(after.getFkIdProcedure().getIdProcedure()).isEqualTo(procedure.getIdProcedure());
        assertThat(after.getPrepared()).isTrue();
        assertThat(after.getReleased()).isTrue();
        assertThat(after.getFlagged()).isTrue();
        assertThat(after.getReentered()).isTrue();
    }

    @Test
    @DisplayName("shouldKeepFlagsWhenOnlyTheNameIsSent")
    void shouldKeepFlagsWhenOnlyTheNameIsSent() throws Exception {
        putJson(stored.getIdSubmittedDocument(), "{\"name\":\"Nombre nuevo\"}", 200);

        SubmittedDocument after = reload();
        assertThat(after.getName()).isEqualTo("Nombre nuevo");
        assertThat(after.getFlagged()).isTrue();
        assertThat(after.getFkIdProcedure().getIdProcedure()).isEqualTo(procedure.getIdProcedure());
    }

    @Test
    @DisplayName("shouldRecomputeTheDueDateWhenTheEntryDateChanges")
    void shouldRecomputeTheDueDateWhenTheEntryDateChanges() throws Exception {
        putJson(stored.getIdSubmittedDocument(), "{\"date\":\"2026-03-01\"}", 200);

        Date expectedDue = Date.from(new SimpleDateFormat("yyyy-MM-dd").parse("2026-03-11").toInstant());
        assertThat(reload().getDateDue()).isEqualTo(expectedDue);
    }

    @Test
    @DisplayName("shouldReturnNotFoundForAnUnknownDocument")
    void shouldReturnNotFoundForAnUnknownDocument() throws Exception {
        putJson(Integer.MAX_VALUE, "{\"delivered\":true}", 404);
    }
}
