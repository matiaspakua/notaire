package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.RequirementCoverage;
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
 * CU08 - Emitir copia impresa de testimonio verificado (issue #832).
 */
@RequirementCoverage({"CU08"})
@DisplayName("ReporteController#generarReporteCopiaTestimonio — CU08 integration tests")
class TestimonioCopiaReportIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createEscrituraFirmada() throws Exception {
        String body = """
                {
                  "numero": %d,
                  "cuerpo": "Escritura firmada para copia de testimonio",
                  "estado": "Firmada",
                  "fechaEscrituracion": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idEscritura").asInt();
    }

    private int generarTestimonio() throws Exception {
        int idEscritura = createEscrituraFirmada();
        MvcResult result = mockMvc.perform(post("/api/v1/testimonio/" + idEscritura + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTestimonio").asInt();
    }

    private int generarTestimonioVerificado() throws Exception {
        int idTestimonio = generarTestimonio();
        mockMvc.perform(post("/api/v1/testimonio/" + idTestimonio + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observado\": false}"))
                .andExpect(status().isOk());
        return idTestimonio;
    }

    @Test
    @DisplayName("Should return a PDF for a verified testimonio")
    void shouldReturnPdfForVerifiedTestimonio() throws Exception {
        int idTestimonio = generarTestimonioVerificado();

        MvcResult result = mockMvc.perform(get("/api/v1/reportes/testimonio/" + idTestimonio + "/copia"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 400 when testimonio is not verified")
    void shouldRejectCopiaForNonVerifiedTestimonio() throws Exception {
        int idTestimonio = generarTestimonio();

        mockMvc.perform(get("/api/v1/reportes/testimonio/" + idTestimonio + "/copia"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for a non-existing testimonio")
    void shouldReturn404ForNonExistingTestimonio() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/testimonio/99999/copia"))
                .andExpect(status().isNotFound());
    }
}
