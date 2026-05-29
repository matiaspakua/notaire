package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Behavioural regression tests for the controllers whose read endpoints previously
 * returned HTTP 500 due to entity/schema drift (testimonio, movimiento-testimonio,
 * tipo-folio, items). Runs on the H2 profile for fast feedback and covers the list
 * and not-found branches. The schema-level guard lives in
 * {@link InitDbSchemaValidationIntegrationTest}.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Previously-broken read endpoints")
class PreviouslyBrokenEndpointsIntegrationTest {

    private static final int MISSING_ID = 999_999;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return a JSON array for GET /api/v1/testimonio")
    void shouldListTestimoniosWhenRequested() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 404 for GET /api/v1/testimonio/{id} when absent")
    void shouldReturnNotFoundWhenTestimonioMissing() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio/" + MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return a JSON array for GET /api/v1/movimiento-testimonio")
    void shouldListMovimientosTestimonioWhenRequested() throws Exception {
        mockMvc.perform(get("/api/v1/movimiento-testimonio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 404 for GET /api/v1/movimiento-testimonio/{id} when absent")
    void shouldReturnNotFoundWhenMovimientoTestimonioMissing() throws Exception {
        mockMvc.perform(get("/api/v1/movimiento-testimonio/" + MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return a JSON array for GET /api/v1/tipo-folio")
    void shouldListTiposDeFolioWhenRequested() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-folio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 404 for GET /api/v1/tipo-folio/{id} when absent")
    void shouldReturnNotFoundWhenTipoDeFolioMissing() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-folio/" + MISSING_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return a JSON array for GET /api/v1/items")
    void shouldListItemsWhenRequested() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 404 for GET /api/v1/items/{id} when absent")
    void shouldReturnNotFoundWhenItemMissing() throws Exception {
        mockMvc.perform(get("/api/v1/items/" + MISSING_ID))
                .andExpect(status().isNotFound());
    }
}
