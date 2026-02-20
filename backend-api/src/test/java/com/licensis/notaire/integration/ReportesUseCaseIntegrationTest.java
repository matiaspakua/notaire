package com.licensis.notaire.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-h2")
class ReportesUseCaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGenerateLibroIndicePdf() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/libro-indice")
                        .param("anio", "2026"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void shouldGenerateDeclaracionJuradaMensualPdf() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026")
                        .param("mes", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void shouldGenerateDeclaracionJuradaRentasPdf() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026")
                        .param("mes", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
    }

    @Test
    void shouldRejectInvalidMonthForDeclaraciones() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                        .param("anio", "2026")
                        .param("mes", "13"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                        .param("anio", "2026")
                        .param("mes", "0"))
                .andExpect(status().isBadRequest());
    }
}
