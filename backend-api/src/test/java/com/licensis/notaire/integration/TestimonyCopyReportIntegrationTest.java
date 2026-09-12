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
@DisplayName("ReportController#generarReporteCopiaTestimonio — CU08 integration tests")
class TestimonyCopyReportIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createDeedFirmada() throws Exception {
        String body = """
                {
                  "number": %d,
                  "body": "Deed firmada para copy de testimony",
                  "status": "Firmada",
                  "dateDeedrecording": "2026-06-16"
                }
                """.formatted((int) (System.currentTimeMillis() % 1_000_000));
        MvcResult result = mockMvc.perform(post("/api/v1/escrituras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idDeed").asInt();
    }

    private int generarTestimony() throws Exception {
        int idDeed = createDeedFirmada();
        MvcResult result = mockMvc.perform(post("/api/v1/testimonio/" + idDeed + "/generar"))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idTestimony").asInt();
    }

    private int generarTestimonyVerified() throws Exception {
        int idTestimony = generarTestimony();
        mockMvc.perform(post("/api/v1/testimonio/" + idTestimony + "/verificar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"flagged\": false}"))
                .andExpect(status().isOk());
        return idTestimony;
    }

    @Test
    @DisplayName("Should return a PDF for a verified testimony")
    void shouldReturnPdfForVerifiedTestimony() throws Exception {
        int idTestimony = generarTestimonyVerified();

        MvcResult result = mockMvc.perform(get("/api/v1/reportes/testimonio/" + idTestimony + "/copia"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return 400 when testimony is not verified")
    void shouldRejectCopyForNonVerifiedTestimony() throws Exception {
        int idTestimony = generarTestimony();

        mockMvc.perform(get("/api/v1/reportes/testimonio/" + idTestimony + "/copia"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 for a non-existing testimony")
    void shouldReturn404ForNonExistingTestimony() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/testimonio/99999/copia"))
                .andExpect(status().isNotFound());
    }
}
