package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Issue #1682 (ADR-029): V50 seeds the standard role template insert-if-absent, so a new
 * installation starts configured and an existing role of the same name keeps the tenant's grants.
 */
@Testcontainers(disabledWithoutDocker = true)
@Tag("pg-integration")
@DisplayName("V50 standard role template migration (issue #1682)")
class RoleTemplateMigrationIntegrationTest {

    private static final Set<String> ALL_MODULES = Set.of("administracion", "usuarios", "tramites",
            "presupuestos", "escrituras", "personas", "auditoria", "workflows");

    @Container
    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("notaire_role_template")
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
                .placeholders(FLYWAY_PLACEHOLDERS)
                .cleanDisabled(false);
        return (target == null ? config : config.target(target)).load();
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword());
    }

    private Set<String> modulesOf(Statement statement, String roleName) throws SQLException {
        Set<String> modules = new HashSet<>();
        try (ResultSet rows = statement.executeQuery("SELECT m.module FROM role_modules m JOIN roles r "
                + "ON r.id = m.fk_id_role WHERE r.name = '" + roleName + "'")) {
            while (rows.next()) {
                modules.add(rows.getString(1));
            }
        }
        return modules;
    }

    @Test
    @DisplayName("a fresh database gets the three template roles and Administrator holds every module")
    void seedsTheTemplateOnAFreshDatabase() throws SQLException {
        flyway(null).clean();
        flyway(null).migrate();

        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            Set<String> names = new HashSet<>();
            try (ResultSet rows = statement.executeQuery("SELECT name FROM roles")) {
                while (rows.next()) {
                    names.add(rows.getString(1));
                }
            }
            assertThat(names).containsExactlyInAnyOrder("Administrator", "Notary", "Office Staff");
            assertThat(modulesOf(statement, "Administrator")).isEqualTo(ALL_MODULES);
            // V53: Office Staff may read catalogs (administracion); writes stay ADMIN-only in Spring Security.
            assertThat(modulesOf(statement, "Office Staff")).isSubsetOf(ALL_MODULES)
                    .contains("administracion")
                    .doesNotContain("usuarios");
        }
    }

    @Test
    @DisplayName("the seeded admin user is linked to the Administrator role")
    void linksTheSeededAdmin() throws SQLException {
        flyway(null).clean();
        flyway(null).migrate();

        try (Connection connection = connection(); Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery("SELECT r.name FROM users u JOIN roles r "
                        + "ON r.id = u.fk_id_role WHERE lower(u.username) = 'admin'")) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getString(1)).isEqualTo("Administrator");
        }
    }

    @Test
    @DisplayName("an existing role of the same name keeps its grants and description")
    void keepsTenantCustomization() throws SQLException {
        flyway(null).clean();
        flyway("49").migrate();

        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO roles (version, name, description, active) "
                    + "VALUES (0, 'Notary', 'tenant description', true)");
            statement.execute("INSERT INTO role_modules (fk_id_role, module) "
                    + "SELECT id, 'personas' FROM roles WHERE name = 'Notary'");

            flyway(null).migrate();

            assertThat(modulesOf(statement, "Notary")).containsExactly("personas");
            try (ResultSet rows = statement.executeQuery(
                    "SELECT description, (SELECT count(*) FROM roles WHERE name = 'Notary') FROM roles "
                            + "WHERE name = 'Notary'")) {
                assertThat(rows.next()).isTrue();
                assertThat(rows.getString(1)).isEqualTo("tenant description");
                assertThat(rows.getInt(2)).isEqualTo(1);
            }
            assertThat(modulesOf(statement, "Administrator")).isEqualTo(ALL_MODULES);
        }
    }
}
