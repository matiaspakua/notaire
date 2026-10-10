package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Issue #1346: V44 maps the stored budget statuses (free text until now: "Pendiente" from the
 * E2E fixtures, "Pending" from Bruno, "BORRADOR" from the UI) to the canonical codes. Unknown
 * values are kept, upper-cased, so no data is invented. Uses the Flyway API on its own container,
 * like {@link BudgetProcedureMigrationGuardIntegrationTest}.
 */
@Testcontainers(disabledWithoutDocker = true)
@Tag("pg-integration")
@DisplayName("V44 budget status vocabulary migration (issue #1346)")
class BudgetStatusMigrationIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("notaire_budget_status")
                    .withUsername("admin")
                    .withPassword("admin");

    private static final Map<String, String> FLYWAY_PLACEHOLDERS = Map.of(
            "exporterUsername", "test_exporter",
            "exporterPassword", "test_exporter_password");

    private Flyway flyway(String target) {
        var config = Flyway.configure()
                .dataSource(POSTGRESQL_CONTAINER.getJdbcUrl(), POSTGRESQL_CONTAINER.getUsername(),
                        POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .placeholders(FLYWAY_PLACEHOLDERS);
        return (target == null ? config : config.target(target)).load();
    }

    @Test
    @DisplayName("maps Spanish and English spellings in any case to the canonical codes")
    void mapsLegacyStatuses() throws SQLException {
        flyway("43").migrate();
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("Pendiente", "PENDIENTE");
        expected.put(" pendiente ", "PENDIENTE");
        expected.put("Pending", "PENDIENTE");
        expected.put("BORRADOR", "BORRADOR");
        expected.put("Draft", "BORRADOR");
        expected.put("Aprobado", "APROBADO");
        expected.put("approved", "APROBADO");
        expected.put("Rechazado", "RECHAZADO");
        expected.put("Rejected", "RECHAZADO");
        expected.put("facturado", "FACTURADO");
        expected.put("Invoiced", "FACTURADO");
        expected.put("Activo", "ACTIVO");

        try (Connection connection = DriverManager.getConnection(POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword());
                Statement statement = connection.createStatement()) {
            int number = 1;
            for (String legacy : expected.keySet()) {
                statement.execute("INSERT INTO budgets (version, number, budget_date, heading, status) VALUES (0, "
                        + number++ + ", CURRENT_DATE, '" + legacy.trim() + "#', '" + legacy + "')");
            }

            flyway(null).migrate();

            Map<String, String> actual = new LinkedHashMap<>();
            try (ResultSet rows = statement.executeQuery("SELECT heading, status FROM budgets ORDER BY number")) {
                while (rows.next()) {
                    actual.put(rows.getString("heading"), rows.getString("status"));
                }
            }
            Map<String, String> byHeading = new LinkedHashMap<>();
            expected.forEach((legacy, canonical) -> byHeading.put(legacy.trim() + "#", canonical));
            assertThat(actual).containsExactlyEntriesOf(byHeading);
        }
    }
}
