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
class ReportesUseCaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // --- Simple PDF endpoints (no DB connection needed) ---

    @Test
    @DisplayName("CU24: libro de índice returns valid PDF bytes")
    void shouldGenerateLibroIndicePdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reportes/libro-indice")
                        .param("anio", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    @Test
    @DisplayName("CU25: declaracion jurada mensual returns valid PDF bytes")
    void shouldGenerateDeclaracionJuradaMensualPdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026")
                        .param("mes", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    @Test
    @DisplayName("CU50: declaracion jurada de rentas returns valid PDF bytes")
    void shouldGenerateDeclaracionJuradaRentasPdf() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026")
                        .param("mes", "6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andReturn();

        byte[] body = result.getResponse().getContentAsByteArray();
        assertThat(body).isNotEmpty();
        assertThat(new String(body, 0, Math.min(5, body.length))).startsWith("%PDF-");
    }

    // --- Month boundary validation ---

    @Test
    @DisplayName("Month 1 and 12 are valid for DDJJ endpoints")
    void shouldAcceptBoundaryMonths() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026").param("mes", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026").param("mes", "12"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026").param("mes", "1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026").param("mes", "12"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Month 0 and 13 are rejected for both DDJJ endpoints")
    void shouldRejectInvalidMonthForDeclaraciones() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026").param("mes", "13"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026").param("mes", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026").param("mes", "0"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026").param("mes", "13"))
                .andExpect(status().isBadRequest());
    }

    // --- Data-backed report endpoints: a missing aggregate is a 404 ---
    // Rendered in-house from the current schema (issue #567); the happy paths
    // are covered by InHouseReportPdfIntegrationTest.

    @Test
    @DisplayName("CU01/CU45: budget endpoint handles missing data gracefully")
    void shouldHandleBudgetEndpointGracefully() throws Exception {
        int nonExistentId = 99999;
        mockMvc.perform(get("/api/v1/reportes/presupuesto/" + nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU01/CU45: budget-inmuebles endpoint handles missing data gracefully")
    void shouldHandleBudgetPropertiesEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU03: lista-documents-tramite endpoint handles unknown tramite gracefully")
    void shouldHandleListaDocumentsProcedureEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite")
                        .param("nombreTipoTramite", "TramiteQueNoExiste"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU13: history-gestion endpoint handles missing gestion gracefully")
    void shouldHandleHistoryManagementEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/historial-gestion/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU42: documents-por-vencer endpoint handles missing document gracefully")
    void shouldHandleDocumentsPorVencerEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/documentos-por-vencer/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CU09: consultar-deuda-documents endpoint handles missing gestion gracefully")
    void shouldHandleConsultarDebtDocumentsEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/consultar-deuda-documentos")
                        .param("numberManagement", "99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Special characters in nombreTipoTramite are matched literally, never a server error")
    void shouldHandleSpecialCharsInProcedureParam() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite")
                        .param("nombreTipoTramite", "Compra/Venta (especial) & más"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("All report endpoints are mapped (not 404)")
    void shouldMapAllReportEndpoints() throws Exception {
        MvcResult[] results = new MvcResult[] {
                mockMvc.perform(get("/api/v1/reportes/presupuesto/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite").param("nombreTipoTramite", "x"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reportes/historial-gestion/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andReturn(),
                mockMvc.perform(get("/api/v1/reportes/consultar-deuda-documentos").param("numberManagement", "1"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reportes/libro-indice").param("anio", "2026"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual").param("anio", "2026").param("mes", "6"))
                        .andReturn(),
                mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas").param("anio", "2026").param("mes", "6"))
                        .andReturn()
        };

        for (MvcResult result : results) {
            assertThat(result.getHandler()).as("Endpoint must be mapped (not 404)").isNotNull();
        }
    }
}
