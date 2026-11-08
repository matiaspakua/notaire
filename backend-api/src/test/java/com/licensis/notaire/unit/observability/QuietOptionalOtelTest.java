package com.licensis.notaire.unit.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Quiet optional OTel and Hibernate deprecation (#1736)")
class QuietOptionalOtelTest {

    private static final Path PROPERTIES = Path.of("src/main/resources/application.properties");
    private static final Path COMPOSE = Path.of("../docker-compose.yml");

    @Test
    @DisplayName("Should disable OTLP metrics and tracing export by default")
    void shouldDisableOtlpExportByDefault() throws IOException {
        String properties = Files.readString(PROPERTIES, StandardCharsets.UTF_8);
        assertThat(properties)
                .contains(
                        "management.otlp.metrics.export.enabled=${MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED:false}")
                .contains(
                        "management.tracing.export.otlp.enabled=${MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED:false}");
    }

    @Test
    @DisplayName("Should silence Hibernate Temporal deprecation at WARN")
    void shouldSilenceHibernateTemporalDeprecationWarn() throws IOException {
        String properties = Files.readString(PROPERTIES, StandardCharsets.UTF_8);
        assertThat(properties).contains("logging.level.org.hibernate.orm.deprecation=ERROR");
    }

    @Test
    @DisplayName("Should enable OTLP export in the app docker-compose stack")
    void shouldEnableOtlpExportInAppCompose() throws IOException {
        String compose = Files.readString(COMPOSE, StandardCharsets.UTF_8);
        assertThat(compose)
                .contains("MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED")
                .contains("MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED");
        assertThat(compose).containsPattern("MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED:\\s*[\"']?true");
        assertThat(compose).containsPattern("MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED:\\s*[\"']?true");
    }
}
