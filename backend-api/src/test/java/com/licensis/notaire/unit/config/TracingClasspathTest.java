package com.licensis.notaire.unit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tracing classpath")
class TracingClasspathTest {

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, TracingClasspathTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Test
    @DisplayName("shouldTraceWithOpenTelemetryBridge")
    void shouldTraceWithOpenTelemetryBridge() {
        assertThat(isPresent("io.micrometer.tracing.otel.bridge.OtelTracer")).isTrue();
    }

    @Test
    @DisplayName("shouldExportSpansOverOtlp")
    void shouldExportSpansOverOtlp() {
        assertThat(isPresent("io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter")).isTrue();
    }

    @Test
    @DisplayName("shouldNotKeepTheLegacyBraveTracer")
    void shouldNotKeepTheLegacyBraveTracer() {
        assertThat(isPresent("io.micrometer.tracing.brave.bridge.BraveTracer")).isFalse();
    }
}
