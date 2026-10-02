package com.licensis.notaire.integration;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * CU69 - Gestión de Inmuebles, against the real Flyway-managed Postgres
 * schema. After #1061, {@code properties.fiscal_valuation} is {@code NUMERIC(19,2)}
 * and {@code Property.fiscalAppraisal} is {@code BigDecimal}. Global Jackson
 * {@code ALWAYS} inclusion means a null appraisal is serialized as JSON null
 * (not omitted).
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Inmueble valuación fiscal contra el esquema real (CU69)")
class PropertyFiscalAppraisalPgIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should create Inmueble with a numeric fiscalAppraisal against the real Postgres schema")
    void shouldCreatePropertyWithNumericFiscalAppraisal() throws Exception {
        String body = """
                {"cadastralDesignation": "NC-879-001", "address": "Calle Falsa 123",
                 "fiscalAppraisal": 150000.5}
                """;

        mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fiscalAppraisal").value(150000.5));
    }

    @Test
    @DisplayName("Should create Inmueble with a null fiscalAppraisal against the real Postgres schema")
    void shouldCreatePropertyWithNullFiscalAppraisal() throws Exception {
        String body = """
                {"cadastralDesignation": "NC-879-002", "address": "Calle Falsa 456"}
                """;

        mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fiscalAppraisal").value(nullValue()));
    }
}
