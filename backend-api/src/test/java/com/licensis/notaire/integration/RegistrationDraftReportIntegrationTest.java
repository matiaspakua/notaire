package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CU82 — printed registration draft from recorded fields (#116).
 */
@RequirementCoverage({"CU82"})
@DisplayName("ReportController#generateRegistrationDraftReport — CU82 integration tests")
class RegistrationDraftReportIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return a PDF with recorded cadastral designation")
    void shouldReturnPdfWithRecordedCadastralDesignation() throws Exception {
        int idDraft = generateDraft("NC-CU82-PRINT");

        MvcResult result = mockMvc.perform(get("/api/v1/reports/registration-draft/" + idDraft))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        byte[] pdf = result.getResponse().getContentAsByteArray();
        assertThat(pdf).isNotEmpty();
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertThat(text).contains("Registration draft");
            assertThat(text).contains("NC-CU82-PRINT");
        }
    }

    @Test
    @DisplayName("Should return 404 for a non-existing registration draft")
    void shouldReturn404ForNonExistingDraft() throws Exception {
        mockMvc.perform(get("/api/v1/reports/registration-draft/99999"))
                .andExpect(status().isNotFound());
    }

    private int generateDraft(String cadastral) throws Exception {
        int idDeed = createSignedDeed();
        int idProperty = createProperty(cadastral);
        int idType = createProcedureType();
        mockMvc.perform(post("/api/v1/procedures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idProcedureType": %d, "idDeed": %d, "idProperty": %d}
                                """.formatted(idType, idDeed, idProperty)))
                .andExpect(status().isCreated());
        MvcResult result = mockMvc.perform(post("/api/v1/registration-drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idDeed\": %d}".formatted(idDeed)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idRegistrationDraft").asInt();
    }

    private int createSignedDeed() throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Signed deed for a registration draft",
                  "status": "Firmada",
                  "dateDeedrecording": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/deeds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idDeed").asInt();
    }

    private int createProperty(String cadastral) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cadastralDesignation": "%s",
                                  "address": "Siempreviva Avenue 742",
                                  "fiscalAppraisal": 100000,
                                  "registrationNumber": "MAT-CU82",
                                  "volumeFolioLandRecord": "Volume 1 Folio 2",
                                  "boundaries": "North, South, East, West"
                                }
                                """.formatted(cadastral)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProperty").asInt();
    }

    private int createProcedureType() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/procedure-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Type CU82 Print", "isRegistered": false,
                                 "isArchived": false, "associatesProperties": true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idProcedureType").asInt();
    }
}
