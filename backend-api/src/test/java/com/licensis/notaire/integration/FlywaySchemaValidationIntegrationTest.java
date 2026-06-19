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
 * Guards against drift between the JPA entities and the Flyway-managed schema
 * that builds the production Docker database.
 *
 * <p>Flyway is the single source of truth for the database schema (V1→V11).
 * Previously the Docker stack used {@code init-db/01-schema.sql} (now archived),
 * which caused 500s when Flyway migrations V3–V10 added columns unknown to the
 * old init-db scripts — but Flyway was dormant in Docker because the init scripts
 * ran first. Now Flyway runs against an empty database and applies all migrations
 * sequentially, matching production behavior.</p>
 *
 * <p>The pre-existing H2 unit tests use {@code ddl-auto=create}, generating the
 * schema from the entities themselves, so they cannot catch column/table drift.
 * This test boots the full application against a PostgreSQL container created
 * from Flyway migrations at production fidelity ({@code ddl-auto=none}),
 * then smoke tests the endpoint families that previously returned 500.</p>
 */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@DisplayName("Flyway Schema vs Entity Validation")
class FlywaySchemaValidationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should boot the application against the Flyway-managed schema")
    void shouldLoadContextAgainstFlywaySchema() {
        // Reaching this point means @SpringBootTest started against a PostgreSQL
        // container whose schema came from Flyway migrations (production fidelity).
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/testimonio against the Flyway schema")
    void shouldListTestimoniosWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/testimonio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/movimiento-testimonio against the Flyway schema")
    void shouldListMovimientosTestimonioWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/movimiento-testimonio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/tipo-folio against the Flyway schema")
    void shouldListTiposDeFolioWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-folio")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/items against the Flyway schema")
    void shouldListItemsWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/items")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for GET /api/v1/personas (Folio to TipoDeFolio join) against the Flyway schema")
    void shouldListPersonasWhenSchemaMatchesEntities() throws Exception {
        mockMvc.perform(get("/api/v1/personas")).andExpect(status().isOk());
    }
}
