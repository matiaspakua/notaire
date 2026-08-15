package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifies the V14 pre-drop guard in isolation: migrates only up to V13, seeds a
 * legacy non-null {@code presupuestos.fk_id_tramite} row by hand (the shape V14
 * refuses to find), then runs the remaining migrations and asserts V14 aborts
 * without dropping the column, rather than silently discarding the row.
 *
 * <p>Uses its own {@link PostgreSQLContainer} and the Flyway Java API directly
 * (not {@link BaseIntegrationTest}/{@code @SpringBootTest}) because Spring Boot's
 * Flyway auto-configuration always migrates to the latest version before the
 * context is available, leaving no point at which to seed data between V13 and
 * V14.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
@Tag("pg-integration")
@DisplayName("V14 migration data-loss guard")
class PresupuestoTramiteMigrationGuardIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("notaire_migration_guard")
                    .withUsername("admin")
                    .withPassword("admin");

    private static final Map<String, String> FLYWAY_PLACEHOLDERS = Map.of(
            "exporterUsername", "test_exporter",
            "exporterPassword", "test_exporter_password");

    @Test
    @DisplayName("V14 refuses to drop presupuestos.fk_id_tramite when a non-null row exists")
    void v14FailsWhenLegacyColumnHoldsData() throws SQLException {
        Flyway.configure()
                .dataSource(POSTGRESQL_CONTAINER.getJdbcUrl(), POSTGRESQL_CONTAINER.getUsername(),
                        POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .placeholders(FLYWAY_PLACEHOLDERS)
                .target("13")
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO tramites (version, numero, nombre) VALUES (0, 1, 'Compraventa')");
            statement.execute(
                    "INSERT INTO presupuestos (version, numero, fecha, encabezado, estado, fk_id_tramite) "
                            + "VALUES (0, 1, CURRENT_DATE, 'Test', 'pendiente', "
                            + "(SELECT id_tramite FROM tramites ORDER BY id_tramite DESC LIMIT 1))");
        }

        Flyway flywayToLatest = Flyway.configure()
                .dataSource(POSTGRESQL_CONTAINER.getJdbcUrl(), POSTGRESQL_CONTAINER.getUsername(),
                        POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .load();

        assertThatThrownBy(flywayToLatest::migrate)
                .isInstanceOf(FlywayException.class)
                .hasMessageContaining("V14 aborted");
    }
}
