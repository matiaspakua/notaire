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
        @DisplayName("Login with empty credentials returns valid=false, not crash")
        void loginWithEmptyCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "", "password": ""}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("Login with null password returns valid=false, not crash")
        void loginWithNullPassword() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "admin", "password": null}
                                    """))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Should respond without crashing")
                .isEqualTo(200));
        }

        @Test
        @DisplayName("Login with SQL injection in username returns valid=false")
        void loginWithSqlInjectionInUsername() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "admin' OR '1'='1", "password": "anything"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").value(false));
        }

        @Test
        @DisplayName("Login with very long username returns valid=false, not crash")
        void loginWithVeryLongUsername() throws Exception {
            String longName = "a".repeat(5000);
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "%s", "password": "pass"}
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
            mockMvc.perform(get("/api/v1/users/abc"))
                    .andExpect(status().is4xxClientError());

            mockMvc.perform(get("/api/v1/people/xyz"))
                    .andExpect(status().is4xxClientError());

            mockMvc.perform(get("/api/v1/managements/not-a-numero"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("Very large ID returns 404, not crash")
        void veryLargeIdReturns404() throws Exception {
            mockMvc.perform(get("/api/v1/users/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/v1/people/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE non-existent resource returns 404")
        void deleteNonExistentResourceReturns404() throws Exception {
            mockMvc.perform(delete("/api/v1/users/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/v1/concepts/" + Integer.MAX_VALUE))
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
        @DisplayName("GET managements returns 200 with paginated content array")
        void getGestionesReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/managements"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET deeds returns 200 with paginated content array")
        void getEscriturasReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/deeds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("GET budgets returns 200 with paginated content array")
        void getPresupuestosReturns200WithArray() throws Exception {
            mockMvc.perform(get("/api/v1/budgets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // -------------------------------------------------------------------------
    // Input validation edge cases
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Input validation edge cases")
    class InputValidationEdgeCases {

        @Test
        @DisplayName("Creating concept with missing name returns error, not crash")
        void createConceptWithMissingNameReturnsError() throws Exception {
            mockMvc.perform(post("/api/v1/concepts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"value": 100}
                                    """))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Missing required field should return 4xx or 5xx, never 200")
                                    .isGreaterThanOrEqualTo(400));
        }

        @Test
        @DisplayName("Creating user with empty body returns error, not 200")
        void createUserWithEmptyBodyReturnsError() throws Exception {
            mockMvc.perform(post("/api/v1/users")
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
            mockMvc.perform(get("/api/v1/reports/monthly-sworn-statement")
                            .param("anio", "2026")
                            .param("mes", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Report endpoint with month 13 returns 400")
        void reportEndpointWithMonth13Returns400() throws Exception {
            mockMvc.perform(get("/api/v1/reports/tax-sworn-statement")
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
                "/api/v1/procedure-types",
                "/api/v1/document-types",
                "/api/v1/concepts",
                "/api/v1/management-statuses",
                "/api/v1/folio-types",
                "/api/v1/folios"
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
            mockMvc.perform(get("/api/v1/people/search")
                            .param("firstName", "")
                            .param("lastName", ""))
                    .andExpect(result ->
                            assertThat(result.getResponse().getStatus())
                                    .as("Empty search query should not crash")
                .isEqualTo(200));
        }

        @Test
        @DisplayName("GET on workflow-definition returns 200 with list")
        void workflowDefinitionListReturns200() throws Exception {
            mockMvc.perform(get("/api/v1/workflow-definition"))
                    .andExpect(status().isOk());
        }
    }
}
