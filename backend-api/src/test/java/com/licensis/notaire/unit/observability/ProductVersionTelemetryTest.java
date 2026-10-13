package com.licensis.notaire.unit.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product version telemetry (#1591)")
class ProductVersionTelemetryTest {

    @Test
    @DisplayName("Should map service.version to the packaged app.version property")
    void shouldDeclareServiceVersionResourceAttribute() throws IOException {
        Path source = Path.of("src/main/resources/application.properties");
        String properties = Files.readString(source, StandardCharsets.UTF_8);
        assertThat(properties).contains("app.version=@project.version@");
        assertThat(properties).contains(
                "management.opentelemetry.resource-attributes.service.version=${app.version}");
    }
}
