package com.licensis.notaire.unit;

import com.licensis.notaire.observability.ApplicationHealthIndicator;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.observability.SharedModuleMetrics;
import com.licensis.notaire.observability.StructuredLogger;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Observability classes - unit tests")
class ObservabilityTest {

    @Nested
    @DisplayName("StructuredLogger")
    class StructuredLoggerTests {

        private StructuredLogger logger;

        @BeforeEach
        void setUp() {
            logger = StructuredLogger.getInstance();
        }

        @Test
        @DisplayName("getInstance should return same singleton")
        void getInstanceShouldReturnSingleton() {
            assertThat(StructuredLogger.getInstance()).isSameAs(logger);
        }

        @Test
        @DisplayName("logTrace should not throw with valid context")
        void logTraceShouldNotThrow() {
            logger.logTrace("trace msg", Map.of("k", "v"));
        }

        @Test
        @DisplayName("logDebug should not throw with valid context")
        void logDebugShouldNotThrow() {
            logger.logDebug("debug msg", Map.of("k", "v"));
        }

        @Test
        @DisplayName("logInfo should not throw")
        void logInfoShouldNotThrow() {
            logger.logInfo("info msg", Map.of("k", "v"));
        }

        @Test
        @DisplayName("logInfo should accept null context")
        void logInfoShouldAcceptNullContext() {
            logger.logInfo("info msg null", null);
        }

        @Test
        @DisplayName("logWarn should not throw")
        void logWarnShouldNotThrow() {
            logger.logWarn("warn msg", Map.of("k", "v"));
        }

        @Test
        @DisplayName("logError should not throw with throwable")
        void logErrorShouldNotThrowWithThrowable() {
            logger.logError("err msg", new RuntimeException("boom"), Map.of("k", "v"));
        }

        @Test
        @DisplayName("logError with empty stacktrace should not fail")
        void logErrorWithEmptyStackShouldNotFail() {
            RuntimeException ex = new RuntimeException("no stack");
            ex.setStackTrace(new StackTraceElement[0]);
            logger.logError("err", ex, Map.of("k", "v"));
        }

        @Test
        @DisplayName("logMetric should not throw")
        void logMetricShouldNotThrow() {
            Map<String, Object> tags = new HashMap<>();
            tags.put("region", "us-east");
            logger.logMetric("request_count", 42.0, tags);
        }

        @Test
        @DisplayName("logAudit should not throw")
        void logAuditShouldNotThrow() {
            logger.logAudit("LOGIN", "admin", "user-1", Map.of("ip", "127.0.0.1"));
        }

        @Test
        @DisplayName("logHealthProbe should not throw")
        void logHealthProbeShouldNotThrow() {
            logger.logHealthProbe("db", "UP", Map.of("latency", 12));
        }
    }

    @Nested
    @DisplayName("MetricsUtil")
    class MetricsUtilTests {

        private MeterRegistry registry;
        private MetricsUtil metrics;

        @BeforeEach
        void setUp() {
            registry = new SimpleMeterRegistry();
            metrics = new MetricsUtil(registry);
        }

        @Test
        @DisplayName("incrementCounter should register and increment the counter")
        void incrementCounterShouldRegisterAndIncrement() {
            metrics.incrementCounter("op1", "success");
            metrics.incrementCounter("op1", "success");
            assertThat(registry.counter("notaire_operation_total", "operation", "op1", "status", "success").count())
                    .isEqualTo(2.0);
        }

        @Test
        @DisplayName("recordDuration should register the timer")
        void recordDurationShouldRegisterTimer() {
            metrics.recordDuration("op2", 100, TimeUnit.MILLISECONDS);
            assertThat(registry.timer("notaire_operation_duration", "operation", "op2").count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("recordDatabaseOperation should register the timer")
        void recordDatabaseOperationShouldRegister() {
            metrics.recordDatabaseOperation("select", "personas", 25);
            assertThat(registry.timer("notaire_database_operation_duration",
                    "operation", "select", "table", "personas").count()).isEqualTo(1L);
        }

        @Test
        @DisplayName("recordApiError should increment the error counter")
        void recordApiErrorShouldIncrementErrorCounter() {
            metrics.recordApiError("/api/v1/x", "VALIDATION", 400);
            assertThat(registry.counter("notaire_api_errors_total",
                    "endpoint", "/api/v1/x", "errorType", "VALIDATION", "status", "400").count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("recordCacheHit and recordCacheMiss should track hits and misses")
        void recordCacheHitsAndMissesShouldTrack() {
            metrics.recordCacheHit("usersCache");
            metrics.recordCacheMiss("usersCache");
            metrics.recordCacheMiss("usersCache");

            assertThat(registry.counter("notaire_cache_hits_total", "cache", "usersCache").count())
                    .isEqualTo(1.0);
            assertThat(registry.counter("notaire_cache_misses_total", "cache", "usersCache").count())
                    .isEqualTo(2.0);
        }

        @Test
        @DisplayName("setActiveOperations should register and update the gauge")
        void setActiveOperationsShouldRegisterGauge() {
            metrics.setActiveOperations("work", 5);
            assertThat(registry.find("notaire_active_operations").tag("operation", "work").gauge().value())
                    .isEqualTo(5.0);

            metrics.setActiveOperations("work", 7);
            assertThat(registry.find("notaire_active_operations").tag("operation", "work").gauge().value())
                    .isEqualTo(7.0);
        }
    }

    @Nested
    @DisplayName("SharedModuleMetrics")
    class SharedModuleMetricsTests {

        private MeterRegistry registry;
        private SharedModuleMetrics shared;

        @BeforeEach
        void setUp() {
            registry = new SimpleMeterRegistry();
            shared = new SharedModuleMetrics(registry);
            shared.init();
        }

        @Test
        @DisplayName("recordSerialization should increment counter and record time")
        void recordSerializationShouldIncrement() {
            shared.recordSerialization(50);
            assertThat(registry.find("notaire_shared_dto_serialization_total").counter().count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("recordDeserialization should increment counter")
        void recordDeserializationShouldIncrement() {
            shared.recordDeserialization(60);
            assertThat(registry.find("notaire_shared_dto_deserialization_total").counter().count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("recordValidation success should not bump error counter")
        void recordValidationSuccessShouldNotIncrementErrors() {
            shared.recordValidation(10, true);
            assertThat(registry.find("notaire_shared_dto_validation_total").counter().count())
                    .isEqualTo(1.0);
            assertThat(registry.find("notaire_shared_validation_errors_total").counter().count())
                    .isEqualTo(0.0);
        }

        @Test
        @DisplayName("recordValidation failure should bump validation error counter")
        void recordValidationFailureShouldIncrementErrors() {
            shared.recordValidation(10, false);
            assertThat(registry.find("notaire_shared_validation_errors_total").counter().count())
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("active DTOs gauge should follow increment/decrement/set")
        void activeDtosGaugeShouldFollowOps() {
            shared.incrementActiveDtos();
            shared.incrementActiveDtos();
            shared.decrementActiveDtos();
            shared.setActiveDtos(10);
            assertThat(registry.find("notaire_shared_active_dtos").gauge().value()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("recordSerializationError should bump serialization error counter")
        void recordSerializationErrorShouldIncrement() {
            shared.recordSerializationError();
            assertThat(registry.find("notaire_shared_serialization_errors_total").counter().count())
                    .isEqualTo(1.0);
        }
    }

    @Nested
    @DisplayName("ApplicationHealthIndicator")
    class ApplicationHealthIndicatorTests {

        @Test
        @DisplayName("health should return UP with details")
        void healthShouldReturnUp() {
            ApplicationHealthIndicator indicator = new ApplicationHealthIndicator();
            Health health = indicator.health();
            assertThat(health.getStatus()).isEqualTo(Status.UP);
            assertThat(health.getDetails()).containsKey("service");
            assertThat(health.getDetails()).containsEntry("service", "notaire-backend");
        }
    }
}
