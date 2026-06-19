package com.licensis.notaire.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests that run against a real PostgreSQL container
 * whose schema is created by Flyway migrations — the same process that builds
 * the production Docker database now that Flyway is the single source of truth.
 *
 * <p>The container starts empty; Flyway applies V1→V11 sequentially, mirroring
 * the production stack. This exercises the actual deployed schema at production
 * fidelity ({@code ddl-auto=none}, same as Docker). Subclass endpoint smoke
 * tests then catch any missing-column / missing-table drift that would produce
 * a runtime 500.</p>
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
            .withPassword("admin");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        // Production fidelity: schema is created by Flyway, Hibernate does not touch it.
        // Subclass endpoint smoke tests catch missing-column/table drift at query time.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        // Schema is created by Flyway migrations (single source of truth).
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        registry.add("spring.flyway.baseline-version", () -> "0");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        // Keep this container-backed context lightweight so it does not starve the
        // rest of the (H2-based) suite of connections/threads when run together.
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "2");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "0");
        // Flyway debug logging to diagnose why it might not be running
        registry.add("logging.level.org.flywaydb", () -> "DEBUG");
    }
}
