package com.licensis.notaire.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

@DisplayName("Logging configuration")
class LoggingConfigurationTest {

    @Test
    @DisplayName("shouldDeclareNoFileAppenderInLogback")
    void shouldDeclareNoFileAppenderInLogback() throws IOException {
        String logback = readResource("logback-spring.xml");

        assertThat(logback).doesNotContain("FileAppender");
    }

    @Test
    @DisplayName("shouldReferenceOnlyTheConsoleAppenderFromTheRootLogger")
    void shouldReferenceOnlyTheConsoleAppenderFromTheRootLogger() throws IOException {
        String logback = readResource("logback-spring.xml");
        String root = logback.substring(logback.indexOf("<root "), logback.indexOf("</root>"));

        assertThat(root).contains("ref=\"CONSOLE\"").doesNotContain("FILE");
    }

    @Test
    @DisplayName("shouldDefineNoLoggingFileKeysInApplicationProperties")
    void shouldDefineNoLoggingFileKeysInApplicationProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = new ClassPathResource("application.properties").getInputStream()) {
            properties.load(input);
        }

        assertThat(properties.stringPropertyNames()).noneMatch(key -> key.startsWith("logging.file."));
    }

    private String readResource(String name) throws IOException {
        try (InputStream input = new ClassPathResource(name).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
