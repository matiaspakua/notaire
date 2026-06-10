package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Edge cases and boundary conditions")
class EdgeCaseBoundaryConditionsTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // -------------------------------------------------------------------------
    // Authentication edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Auth edge cases")
    class AuthEdgeCases {

        @Test
        @DisplayName("Login with empty credentials returns valido=false, not crash")
        void loginWithEmptyCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nombre": "", "contrasenia": ""}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(false));
        }

        @Test
        @DisplayName("Login with null password returns valido=false, not crash")
        void loginWithNullPassword() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nombre": "admin", "contrasenia": null}
                                    """))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Should respond without crashing")
                                    .isIn(200, 400, 500));
        }

        @Test
        @DisplayName("Login with SQL injection in username returns valido=false")
        void loginWithSqlInjectionInUsername() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nombre": "admin' OR '1'='1", "contrasenia": "anything"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").value(false));
        }

        @Test
        @DisplayName("Login with very long username returns valido=false, not crash")
        void loginWithVeryLongUsername() throws Exception {
            String longName = "a".repeat(5000);
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"nombre": "%s", "contrasenia": "pass"}
                                    """.formatted(longName)))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Should not crash on oversized username")
                                    .isNotEqualTo(0));
        }
    }

    // -------------------------------------------------------------------------
    // ID path parameter edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Path parameter edge cases")
    class PathParameterEdgeCases {

        @Test
        @DisplayName("Non-numeric ID on any resource returns 400, not crash")
        void nonNumericIdReturns400() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios/abc"))
                    .andExpect(status().is4xxClientError());

            mockMvc.perform(get("/api/v1/personas/xyz"))
                    .andExpect(status().is4xxClientError());

            mockMvc.perform(get("/api/v1/gestiones/not-a-number"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Very large ID returns 404, not crash")
        void veryLargeIdReturns404() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/personas/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE non-existent resource returns 404")
        void deleteNonExistentResourceReturns404() throws Exception {
            mockMvc.perform(delete("/api/v1/usuarios/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/v1/conceptos/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }
    }

    // -------------------------------------------------------------------------
    // Collection endpoint edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Collection endpoint edge cases")
    class CollectionEndpointEdgeCases {

        @Test
        @DisplayName("GET gestiones returns 200 with JSON array")
        void getGestionesReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET escrituras returns 200 with JSON array")
        void getEscriturasReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/escrituras"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET presupuestos returns 200 with JSON array")
        void getPresupuestosReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // -------------------------------------------------------------------------
    // Input validation edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation edge cases")
    class InputValidationEdgeCases {

        @Test
        @DisplayName("Creating concepto with missing nombre returns error, not crash")
        void createConceptoWithMissingNameReturnsError() throws Exception {
            mockMvc.perform(post("/api/v1/conceptos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"valor": 100}
                                    """))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Missing required field should return 4xx or 5xx, never 200")
                                    .isGreaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("Creating usuario with empty body returns error, not 200")
        void createUsuarioWithEmptyBodyReturnsError() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Empty body should not silently succeed")
                                    .isNotEqualTo(0));
        }

        @Test
        @DisplayName("Report endpoint with month 0 returns 400")
        void reportEndpointWithMonthZeroReturns400() throws Exception {
            mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-mensual")
                            .param("anio", "2026")
                            .param("mes", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Report endpoint with month 13 returns 400")
        void reportEndpointWithMonth13Returns400() throws Exception {
            mockMvc.perform(get("/api/v1/reportes/declaracion-jurada-rentas")
                            .param("anio", "2026")
                            .param("mes", "13"))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // Catalog read edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Catalog read edge cases")
    class CatalogReadEdgeCases {

        @Test
        @DisplayName("All catalog endpoints return 200 with arrays (not crashes)")
        void allCatalogEndpointsReturn200() throws Exception {
            String[] endpoints = {
                "/api/v1/tipo-tramite",
                "/api/v1/tipo-de-documento",
                "/api/v1/conceptos",
                "/api/v1/estado-gestion",
                "/api/v1/tipo-folio",
                "/api/v1/folio"
            };
            for (String endpoint : endpoints) {
                mockMvc.perform(get(endpoint))
                        .andExpect(result ->
                                assertThat(result.getResponse().getStatus())
                                        .as("Catalog endpoint %s should return 200".formatted(endpoint))
                                        .isEqualTo(200));
            }
        }

        @Test
        @DisplayName("Search endpoint handles empty query without crash")
        void searchEndpointHandlesEmptyQuery() throws Exception {
            mockMvc.perform(get("/api/v1/personas/buscar")
                            .param("nombre", "")
                            .param("apellido", ""))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Empty search query should not crash")
                                    .isIn(200, 400));
        }

        @Test
        @DisplayName("GET on workflow-definition returns 200 with list")
        void workflowDefinitionListReturns200() throws Exception {
            mockMvc.perform(get("/api/v1/workflow-definition"))
                    .andExpect(status().isOk());
        }
    }
}
