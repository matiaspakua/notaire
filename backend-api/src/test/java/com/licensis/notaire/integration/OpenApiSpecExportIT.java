package com.licensis.notaire.integration;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Optional OpenAPI exporter for issue #1067. Skipped in normal CI suites;
 * enabled by {@code -Dopenapi.export.path=...} via {@code scripts/export-openapi.sh}.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("OpenAPI spec export (issue #1067)")
class OpenApiSpecExportIT {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Writes springdoc YAML to openapi.export.path when set")
    void shouldExportOpenApiYamlWhenPathPropertySet() throws Exception {
        String exportPath = System.getProperty("openapi.export.path");
        Assumptions.assumeTrue(
                exportPath != null && !exportPath.isBlank(),
                "Set -Dopenapi.export.path to enable export");

        byte[] body = mockMvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(body).isNotEmpty();
        Path target = Path.of(exportPath);
        Files.createDirectories(target.getParent());
        Files.write(target, body);
        String text = new String(body, StandardCharsets.UTF_8);
        assertThat(text).contains("openapi:");
        assertThat(text).contains("Notaire");
    }
}
