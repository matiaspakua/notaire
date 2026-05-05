package com.licensis.notaire.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = 
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("notaire_test")
            .withUsername("admin")
            .withPassword("admin")
            .withInitScript("init-db/01-schema.sql")
            .withInitScript("init-db/02-data.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRESQL_CONTAINER::getDriverClassName);
        // Hibernate validates schema (created by init scripts)
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        // Disable Flyway in tests (scripts run via Testcontainers)
        registry.add("spring.flyway.enabled", () -> "false");
    }
}
