package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Product version on the OpenTelemetry resource (#1591)")
class ProductVersionTelemetryIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    @DisplayName("Should resolve service.version from the filtered app.version at runtime")
    void shouldResolveServiceVersionFromAppVersion() {
        String appVersion = environment.getProperty("app.version");
        assertThat(appVersion).isNotBlank().doesNotContain("@project");

        String serviceVersion = environment.getProperty(
                "management.opentelemetry.resource-attributes.service.version");
        assertThat(serviceVersion).isEqualTo(appVersion);
    }
}
