package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Prometheus metrics integration tests")
class PrometheusMetricsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Prometheus endpoint is accessible and returns metrics text")
    void prometheusEndpointReturnsMetrics() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    @DisplayName("Prometheus response contains JVM metrics")
    void prometheusContainsJvmMetrics() throws Exception {
        String body = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("jvm_memory_used_bytes");
        assertThat(body).contains("jvm_threads_live_threads");
    }

    @Test
    @DisplayName("Prometheus response contains process and system metrics")
    void prometheusContainsProcessMetrics() throws Exception {
        String body = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body).containsAnyOf("process_uptime_seconds", "system_cpu_usage",
                "logback_events_total", "jvm_gc_pause_seconds");
    }

    @Test
    @DisplayName("Prometheus response contains notaire login attempt counters after login")
    void prometheusContainsLoginMetricsAfterLogin() throws Exception {
        // Trigger a successful login
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andExpect(status().isOk());

        // Trigger a failed login
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "wrong"}
                                """))
                .andExpect(status().isOk());

        String body = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(body)
                .as("Login attempt counter should be present in Prometheus output")
                .contains("notaire_operation_total")
                .contains("login");
    }
}
