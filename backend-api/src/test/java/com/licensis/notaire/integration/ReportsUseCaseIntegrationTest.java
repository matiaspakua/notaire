package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
class ReportsUseCaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // --- Simple PDF endpoints (no DB connection needed) ---

    @Test
    @DisplayName("CU24: the index book returns valid PDF bytes")
    void shouldGenerateIndexBookPdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reports/index-book")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    @Test
    @DisplayName("CU25: monthly sworn statement returns valid PDF bytes")
    void shouldGenerateMonthlySwornStatementPdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    @Test
    @DisplayName("CU50: tax sworn statement returns valid PDF bytes")
    void shouldGenerateTaxSwornStatementPdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
                        .param("year", "2026")
                        .param("month", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    // --- Month boundary validation ---

    @Test
    @DisplayName("Month 1 and 12 are valid for sworn-statement endpoints")
    void shouldAcceptBoundaryMonths() throws Exception {
        mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                        .param("year", "2026").param("month", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                        .param("year", "2026").param("month", "12"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
                        .param("year", "2026").param("month", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
                        .param("year", "2026").param("month", "12"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Month 0 and 13 are rejected for both sworn-statement endpoints")
    void shouldRejectInvalidMonthForSwornStatements() throws Exception {
        mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                        .param("year", "2026").param("month", "13"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                        .param("year", "2026").param("month", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
                        .param("year", "2026").param("month", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
                        .param("year", "2026").param("month", "13"))
                .andExpect(status().isBadRequest());
    }

    // --- Data-backed report endpoints: a missing aggregate is a 404 ---
    // Rendered in-house from the current schema (issue #567); the happy paths
    // are covered by InHouseReportPdfIntegrationTest.

    @Test
    @DisplayName("CU01/CU45: budget endpoint handles missing data gracefully")
    void shouldHandleBudgetEndpointGracefully() throws Exception {
        int nonExistentId = 99999;
        mockMvc.perform(get("/api/v1/reports/budget/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU01/CU45: budget-properties endpoint handles missing data gracefully")
    void shouldHandleBudgetPropertiesEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/budget-properties/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU03: procedure-document list endpoint handles unknown procedure gracefully")
    void shouldHandleProcedureDocumentListEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/procedure-document-list")
                        .param("procedureTypeName", "ProcedureTypeThatDoesNotExist"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU13: management-history endpoint handles missing management gracefully")
    void shouldHandleHistoryManagementEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/management-history/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU42: documents-due endpoint handles missing document gracefully")
    void shouldHandleDocumentsDueEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/documents-due/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU09: document-debt endpoint handles a missing management gracefully")
    void shouldHandleDocumentDebtEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reports/document-debt")
                        .param("numberManagement", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Special characters in procedureTypeName are matched literally, never a server error")
    void shouldHandleSpecialCharsInProcedureParam() throws Exception {
        mockMvc.perform(get("/api/v1/reports/procedure-document-list")
                        .param("procedureTypeName", "Compra/Venta (especial) & more"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("All report endpoints are mapped (not 404)")
    void shouldMapAllReportEndpoints() throws Exception {
        MvcResult[] results = new MvcResult[] {
                mockMvc.perform(get("/api/v1/reports/budget/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reports/budget-properties/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reports/procedure-document-list").param("procedureTypeName", "x"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reports/management-history/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reports/documents-due/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reports/document-debt").param("numberManagement", "1"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reports/index-book").param("year", "2026"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement").param("year", "2026").param("month", "6"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reports/tax-sworn-statement").param("year", "2026").param("month", "6"))
                        .andReturn()
        };

        for (MvcResult result : results) {
            assertThat(result.getHandler()).as("Endpoint must be mapped (not 404)").isNotNull();
        }
    }
}
