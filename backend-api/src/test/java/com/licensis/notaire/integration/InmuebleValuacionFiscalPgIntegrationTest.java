package com.licensis.notaire.integration;

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
 * schema. The {@code inmuebles.valuacion_fiscal} column is {@code real};
 * H2-based tests (e.g. {@code InmuebleRepositoryIntegrationTest}) do not
 * enforce this type strictness, which is why they stay green despite
 * {@code Inmueble.valuacionFiscal} being declared {@code String} and every
 * real Postgres INSERT failing with a type-mismatch error (Issue #879).
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Inmueble valuación fiscal contra el esquema real (CU69)")
class InmuebleValuacionFiscalPgIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should create Inmueble with a numeric valuacionFiscal against the real Postgres schema")
    void shouldCreateInmuebleWithNumericValuacionFiscal() throws Exception {
        String body = """
                {"nomenclaturaCatastral": "NC-879-001", "domicilio": "Calle Falsa 123",
                 "valuacionFiscal": 150000.5}
                """;

        mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valuacionFiscal").value(150000.5));
    }

    @Test
    @DisplayName("Should create Inmueble with a null valuacionFiscal against the real Postgres schema")
    void shouldCreateInmuebleWithNullValuacionFiscal() throws Exception {
        String body = """
                {"nomenclaturaCatastral": "NC-879-002", "domicilio": "Calle Falsa 456"}
                """;

        mockMvc.perform(post("/api/v1/inmueble")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valuacionFiscal").doesNotExist());
    }
}
