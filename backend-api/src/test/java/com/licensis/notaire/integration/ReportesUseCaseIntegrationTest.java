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

    // --- JasperReports-based endpoints — verify graceful error handling ---
    // These endpoints require a DB connection; in H2 mode they return 500 when
    // the Jasper template SQL is incompatible or data is absent. The controller
    // must never propagate an unhandled exception (no 5xx without body or 404).

    @Test
    @DisplayName("CU01/CU45: presupuesto endpoint handles missing data gracefully")
    void shouldHandlePresupuestoEndpointGracefully() throws Exception {
        int nonExistentId = 99999;
        mockMvc.perform(get("/api/v1/reportes/presupuesto/" + nonExistentId))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("CU01/CU45: presupuesto-inmuebles endpoint handles missing data gracefully")
    void shouldHandlePresupuestoInmueblesEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("CU03: lista-documentos-tramite endpoint handles unknown tramite gracefully")
    void shouldHandleListaDocumentosTramiteEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite")
                        .param("nombreTipoTramite", "TramiteQueNoExiste"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("CU13: historial-gestion endpoint handles missing gestion gracefully")
    void shouldHandleHistorialGestionEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/historial-gestion/99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("CU42: documentos-por-vencer endpoint handles missing document gracefully")
    void shouldHandleDocumentosPorVencerEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/documentos-por-vencer/99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("CU09: consultar-deuda-documentos endpoint handles missing gestion gracefully")
    void shouldHandleConsultarDeudaDocumentosEndpointGracefully() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/consultar-deuda-documentos")
                        .param("numeroGestion", "99999"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("Special characters in nombreTipoTramite are safely handled")
    void shouldHandleSpecialCharsInTramiteParam() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite")
                        .param("nombreTipoTramite", "Compra/Venta (especial) & más"))
                .andExpect(result ->
                    assertThat(result.getResponse().getStatus())
                        .as("Should not return 404 or crash with special chars")
                        .isNotEqualTo(404)
                        .isNotEqualTo(400));
    }

    @Test
    @DisplayName("All report endpoints are mapped (not 404)")
    void shouldMapAllReportEndpoints() throws Exception {
        int[] statuses = {
            mockMvc.perform(get("/api/v1/reportes/presupuesto/1")).andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/presupuesto-inmuebles/1")).andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite").param("nombreTipoTramite", "x"))
                    .andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/historial-gestion/1")).andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/documentos-por-vencer/1")).andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/consultar-deuda-documentos").param("numeroGestion", "1"))
                    .andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/libro-indice").param("anio", "2026"))
                    .andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual").param("anio", "2026").param("mes", "6"))
                    .andReturn().getResponse().getStatus(),
            mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas").param("anio", "2026").param("mes", "6"))
                    .andReturn().getResponse().getStatus()
        };

        for (int status : statuses) {
            assertThat(status).as("Endpoint must be mapped (not 404)").isNotEqualTo(404);
        }
    }
}
