package com.licensis.notaire.unit.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@DisplayName("Product version log identity (#1591)")
class ProductVersionLogIdentityTest {

    @Test
    @DisplayName("Should emit serviceVersion from app.version without per-request fields")
    void shouldDeclareServiceVersionInLogback() throws IOException {
        String logback = readResource("logback-spring.xml");
        assertThat(logback).contains("<springProperty scope=\"context\" name=\"APP_VERSION\" source=\"app.version\"/>");
        assertThat(logback).contains("\"serviceVersion\":\"${APP_VERSION}\"");
        assertThat(logback).doesNotContain("request");
    }

    private String readResource(String name) throws IOException {
        try (InputStream input = new ClassPathResource(name).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
