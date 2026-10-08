package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DocumentType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DocumentTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.SubmittedDocumentRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

/** CU42 - Informar próximos vencimientos (issue #802): window query over submitted documents. */
@RequirementCoverage({"CU42"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Próximos vencimientos — CU42 integration tests")
class UpcomingExpirationControllerIntegrationTest {

    private static final String ENDPOINT = "/api/v1/documento-presentado/proximos-vencimientos";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DocumentTypeRepository documentTypeRepository;

    @Autowired
    private SubmittedDocumentRepository submittedDocumentRepository;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private Integer procedureId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        ProcedureType procedureType = new ProcedureType();
        procedureType.setName("Tipo trámite CU42 " + System.nanoTime());
        procedureType.setEnabled(true);
        procedureType = procedureTypeRepository.save(procedureType);
        Procedure procedure = new Procedure();
        procedure.setFkIdProcedureType(procedureType);
        procedureId = procedureRepository.save(procedure).getIdProcedure();
    }

    private Integer documentType(boolean expires, Integer dueDays) {
        DocumentType type = new DocumentType();
        type.setName("Tipo CU42 " + System.nanoTime());
        type.setExpires(expires);
        type.setDueDays(dueDays);
        type.setDeliveredBy("");
        type.setEnabled(true);
        return documentTypeRepository.save(type).getIdDocumentType();
    }

    private int submit(Integer typeId, LocalDate entryDate) throws Exception {
        String body = """
                {"typeId": %d, "procedureId": %d, "date": "%s", "delivered": false}
                """.formatted(typeId, procedureId, entryDate);
        String response = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(response).get("idSubmittedDocument").asInt();
    }

    private List<Integer> upcomingIds(String query) throws Exception {
        String response = mockMvc.perform(get(ENDPOINT + query))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Integer> ids = new ArrayList<>();
        for (JsonNode row : mapper.readTree(response)) {
            ids.add(row.get("idSubmittedDocument").asInt());
        }
        return ids;
    }

    @Test
    @DisplayName("Should list documents due inside the window ordered by due date, and skip the rest")
    void shouldListOnlyDocumentsInsideTheWindow() throws Exception {
        LocalDate today = LocalDate.now();
        int dueIn20 = submit(documentType(true, 20), today);
        int dueIn5 = submit(documentType(true, 5), today);
        int dueBeyond = submit(documentType(true, 90), today);
        int overdue = submit(documentType(true, 3), today.minusDays(10));
        int notExpiring = submit(documentType(false, null), today);
        int released = submit(documentType(true, 7), today);
        submittedDocumentRepository.findById(released).ifPresent(d -> {
            d.setReleased(true);
            submittedDocumentRepository.save(d);
        });

        List<Integer> ids = upcomingIds("?dias=30");

        assertThat(ids).contains(dueIn5, dueIn20);
        assertThat(ids.indexOf(dueIn5)).isLessThan(ids.indexOf(dueIn20));
        assertThat(ids).doesNotContain(dueBeyond, overdue, notExpiring, released);
    }

    @Test
    @DisplayName("Should apply a 30-day window when dias is omitted")
    void shouldDefaultTheWindow() throws Exception {
        int dueIn25 = submit(documentType(true, 25), LocalDate.now());
        int dueIn45 = submit(documentType(true, 45), LocalDate.now());

        List<Integer> ids = upcomingIds("");

        assertThat(ids).contains(dueIn25).doesNotContain(dueIn45);
    }

    @Test
    @DisplayName("Should return 400 for a window outside 1 to 365 days")
    void shouldRejectAnInvalidWindow() throws Exception {
        mockMvc.perform(get(ENDPOINT + "?dias=0")).andExpect(status().isBadRequest());
        mockMvc.perform(get(ENDPOINT + "?dias=366")).andExpect(status().isBadRequest());
    }
}
