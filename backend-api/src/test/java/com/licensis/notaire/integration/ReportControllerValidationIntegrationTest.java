package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Reporte controller request validation (issue #561)")
class ReportControllerValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("GET budget report with non-positive idBudget returns 400")
    void shouldRejectBudgetReportWithNonPositiveId() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/presupuesto/0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET budget report with negative idBudget returns 400")
    void shouldRejectBudgetReportWithNegativeId() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/presupuesto/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET lista-documents-tramite with blank nombreTipoTramite returns 400")
    void shouldRejectListaDocumentsWithBlankNameTypeProcedure() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/lista-documentos-tramite")
                        .param("nombreTipoTramite", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET declaracion-jurada-mensual with mes=13 returns 400")
    void shouldRejectDeclaracionJuradaMensualWithMesOutOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026")
                        .param("mes", "13"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET declaracion-jurada-mensual with non-positive anio returns 400")
    void shouldRejectDeclaracionJuradaMensualWithNonPositiveYear() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "0")
                        .param("mes", "6"))
                .andExpect(status().isBadRequest());
    }
}
