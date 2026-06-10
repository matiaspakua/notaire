package com.licensis.notaire.unit;

import com.licensis.notaire.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenAPI Configuration")
class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    @DisplayName("Should configure API info with title and version")
    void shouldConfigureApiInfo() {
        OpenAPI api = config.customOpenAPI();
        assertThat(api.getInfo()).isNotNull();
        assertThat(api.getInfo().getTitle()).isEqualTo("Notaire API");
        assertThat(api.getInfo().getVersion()).isEqualTo("1.0.0");
    }

    @Test
    @DisplayName("Should configure contact information")
    void shouldConfigureContact() {
        OpenAPI api = config.customOpenAPI();
        assertThat(api.getInfo().getContact()).isNotNull();
        assertThat(api.getInfo().getContact().getEmail()).isEqualTo("info@licensis.com");
    }

    @Test
    @DisplayName("Should configure server entries")
    void shouldConfigureServers() {
        OpenAPI api = config.customOpenAPI();
        assertThat(api.getServers()).isNotEmpty();
        assertThat(api.getServers()).anyMatch(s -> s.getUrl().contains("localhost:8080"));
    }

    @Test
    @DisplayName("Should define common error response components")
    void shouldDefineCommonResponseComponents() {
        OpenAPI api = config.customOpenAPI();
        assertThat(api.getComponents()).isNotNull();
        assertThat(api.getComponents().getResponses()).containsKeys("400", "404", "409", "500");
    }

    @Test
    @DisplayName("Should include description")
    void shouldIncludeDescription() {
        OpenAPI api = config.customOpenAPI();
        assertThat(api.getInfo().getDescription()).isNotBlank();
    }
}
