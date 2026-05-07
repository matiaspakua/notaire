package com.licensis.notaire.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metrics collector for notaire-shared module operations.
 * Tracks DTO usage, serialization, and shared module performance
 * for visibility in Prometheus and Grafana dashboards.
 */
@Component
public class SharedModuleMetrics {

    private static final Logger logger = LoggerFactory.getLogger(SharedModuleMetrics.class);
    private static final String METRIC_PREFIX = "notaire_shared";

    private final MeterRegistry meterRegistry;

    // DTO Usage Counters
    private Counter dtoSerializationCounter;
    private Counter dtoDeserializationCounter;
    private Counter dtoValidationCounter;

    // Error Counter
    private Counter serializationErrorCounter;
    private Counter validationErrorCounter;

    // Active DTOs gauge
    private final AtomicInteger activeDtos = new AtomicInteger(0);

    // Timers
    private Timer serializationTimer;
    private Timer deserializationTimer;
    private Timer validationTimer;

    public SharedModuleMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // DTO Usage Counters
        dtoSerializationCounter = Counter.builder(METRIC_PREFIX + "_dto_serialization_total")
            .description("Total DTO serialization operations from notaire-shared")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        dtoDeserializationCounter = Counter.builder(METRIC_PREFIX + "_dto_deserialization_total")
            .description("Total DTO deserialization operations from notaire-shared")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        dtoValidationCounter = Counter.builder(METRIC_PREFIX + "_dto_validation_total")
            .description("Total DTO validation operations from notaire-shared")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        // Error Counters
        serializationErrorCounter = Counter.builder(METRIC_PREFIX + "_serialization_errors_total")
            .description("Total serialization errors from notaire-shared")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        validationErrorCounter = Counter.builder(METRIC_PREFIX + "_validation_errors_total")
            .description("Total validation errors from notaire-shared")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        // Timers
        serializationTimer = Timer.builder(METRIC_PREFIX + "_serialization_duration")
            .description("DTO serialization duration")
            .tag("module", "notaire-shared")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        deserializationTimer = Timer.builder(METRIC_PREFIX + "_deserialization_duration")
            .description("DTO deserialization duration")
            .tag("module", "notaire-shared")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        validationTimer = Timer.builder(METRIC_PREFIX + "_validation_duration")
            .description("DTO validation duration")
            .tag("module", "notaire-shared")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);

        // Active DTOs Gauge
        io.micrometer.core.instrument.Gauge.builder(METRIC_PREFIX + "_active_dtos", activeDtos, AtomicInteger::get)
            .description("Number of currently active DTOs in use")
            .tag("module", "notaire-shared")
            .register(meterRegistry);

        logger.info("SharedModuleMetrics initialized - tracking notaire-shared DTO operations");
    }

    /**
     * Record a DTO serialization operation
     */
    public void recordSerialization(long durationMillis) {
        dtoSerializationCounter.increment();
        serializationTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Record a DTO deserialization operation
     */
    public void recordDeserialization(long durationMillis) {
        dtoDeserializationCounter.increment();
        deserializationTimer.record(durationMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Record a DTO validation operation
     */
    public void recordValidation(long durationMillis, boolean success) {
        dtoValidationCounter.increment();
        validationTimer.record(durationMillis, TimeUnit.MILLISECONDS);
        if (!success) {
            validationErrorCounter.increment();
        }
    }

    /**
     * Track active DTO count
     */
    public void incrementActiveDtos() {
        activeDtos.incrementAndGet();
    }

    /**
     * Track active DTO count decrease
     */
    public void decrementActiveDtos() {
        activeDtos.decrementAndGet();
    }

    /**
     * Set active DTO count
     */
    public void setActiveDtos(int count) {
        activeDtos.set(count);
    }

    /**
     * Record a serialization error
     */
    public void recordSerializationError() {
        serializationErrorCounter.increment();
    }
}
