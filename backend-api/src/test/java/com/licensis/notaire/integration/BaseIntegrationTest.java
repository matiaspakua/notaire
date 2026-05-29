package com.licensis.notaire.integration;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

/**
 * Base class for integration tests that run against a real PostgreSQL container
 * whose schema is created from the project's {@code init-db} SQL scripts — the
 * same scripts that build the production Docker database.
 *
 * <p>This is deliberate: it exercises the actual deployed schema (not a schema
 * generated from the JPA entities), at production fidelity ({@code ddl-auto=none},
 * same as Docker). Endpoint smoke tests in subclasses then fail on any
 * missing-column / missing-table drift that would produce a runtime 500 — the
 * exact failure class that previously broke testimonio, movimiento-testimonio,
 * tipo-folio and items. The scripts are mounted into {@code /docker-entrypoint-initdb.d}
 * (which supports multiple ordered files, unlike {@code withInitScript}).</p>
 *
 * <p>Requires Docker. Tests are skipped automatically when Docker is unavailable.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notaire_test")
            .withUsername("admin")
            .withPassword("admin")
            .withCopyFileToContainer(
                MountableFile.forHostPath(initDbScript("01-schema.sql")),
                "/docker-entrypoint-initdb.d/01-schema.sql")
            .withCopyFileToContainer(
                MountableFile.forHostPath(initDbScript("02-data.sql")),
                "/docker-entrypoint-initdb.d/02-data.sql");

    /**
     * Resolve an {@code init-db} script on the host filesystem, walking up from the
     * working directory so the path works whether the build runs from the module
     * directory or the repository root.
     *
     * @param fileName the script file name (e.g. {@code 01-schema.sql})
     * @return absolute path to the script
     */
    private static Path initDbScript(String fileName) {
        Path dir = Paths.get("").toAbsolutePath();
        for (int depth = 0; depth < 5 && dir != null; depth++) {
            Path candidate = dir.resolve("init-db").resolve(fileName);
            if (candidate.toFile().exists()) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("Could not locate init-db/" + fileName + " from " + Paths.get("").toAbsolutePath());
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        // Production fidelity: schema comes from init-db, Hibernate does not touch it.
        // Subclass endpoint smoke tests catch missing-column/table drift at query time.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        // Schema is created by the init scripts, not Flyway.
        registry.add("spring.flyway.enabled", () -> "false");
        // Keep this container-backed context lightweight so it does not starve the
        // rest of the (H2-based) suite of connections/threads when run together.
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "0");
    }
}
