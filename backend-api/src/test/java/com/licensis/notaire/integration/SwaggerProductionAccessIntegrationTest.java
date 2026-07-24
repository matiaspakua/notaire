package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@TestPropertySource(properties = {
        "app.environment=production",
        "app.admin.username=test-prod-admin",
        "app.admin.password=test-prod-admin-pass",
        "pgadmin.admin.password=test-prod-pgadmin-pass",
        "grafana.admin.username=test-prod-grafana-user",
        "grafana.admin.password=test-prod-grafana-pass"
})
@DisplayName("Swagger/OpenAPI access is blocked in production (issue #671)")
class SwaggerProductionAccessIntegrationTest {

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
    @DisplayName("Swagger UI HTML is not accessible without credentials in production")
    void shouldBlockSwaggerUiInProduction() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Swagger UI resources are not accessible without credentials in production")
    void shouldBlockSwaggerUiResourcesInProduction() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("OpenAPI JSON spec is not accessible without credentials in production")
    void shouldBlockApiDocsInProduction() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is4xxClientError());
    }
}
