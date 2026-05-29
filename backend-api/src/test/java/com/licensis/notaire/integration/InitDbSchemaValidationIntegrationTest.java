package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Guards against drift between the JPA entities and {@code init-db/01-schema.sql},
 * the schema that builds the production Docker database.
 *
 * <p>The Docker database is created exclusively from the {@code init-db} scripts
 * (Flyway is dormant in Docker). Previously every read of an endpoint whose entity
 * referenced a column missing from {@code init-db} failed with HTTP 500
 * (InvalidDataAccessResourceUsageException). The pre-existing H2 tests could not
 * catch this because they use {@code ddl-auto=create}, generating the schema from
 * the entities themselves.</p>
 *
 * <p>This test boots the full application against a PostgreSQL container created
 * from the real {@code init-db} scripts at production fidelity ({@code ddl-auto=none}),
 * then smoke tests the endpoint families that previously returned 500. A missing
 * column or table reintroduced into the entities (or removed from init-db) makes
 * the corresponding request return 500 and fails the build.</p>
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("init-db Schema vs Entity Validation")
class InitDbSchemaValidationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should boot the application against the real init-db schema")
    void shouldLoadContextAgainstInitDbSchema() {
        // Reaching this point means @SpringBootTest started against a PostgreSQL
        // container whose schema came from init-db/01-schema.sql (production fidelity).
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/testimonio against the real schema")
    void shouldListTestimoniosWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/movimiento-testimonio against the real schema")
    void shouldListMovimientosTestimonioWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/movimiento-testimonio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/tipo-folio against the real schema")
    void shouldListTiposDeFolioWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-folio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/items against the real schema")
    void shouldListItemsWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/items")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/personas (Folio to TipoDeFolio join) against the real schema")
    void shouldListPersonasWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/personas")).andExpect(status().isOk());
    }
}
