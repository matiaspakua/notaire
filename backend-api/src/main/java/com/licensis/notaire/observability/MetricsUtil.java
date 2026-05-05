package com.licensis.notaire.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Metrics utility for application observability
 * Integrates with Micrometer for Prometheus metrics
 */
@Component
public class MetricsUtil {

    private final MeterRegistry meterRegistry;
    private static final String APP_NAMESPACE = "notaire";

    public MetricsUtil(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Increment a counter for an operation
     */
    public void incrementCounter(String operation, String status) {
        Counter.builder(APP_NAMESPACE + "_operation_total")
            .description("Total operations count")
            .tag("operation", operation)
            .tag("status", status)
            .register(meterRegistry)
            .increment();
    }

    /**
     * Record the duration of an operation
     */
    public void recordDuration(String operation, long duration, TimeUnit unit) {
        Timer.builder(APP_NAMESPACE + "_operation_duration")
            .description("Operation duration")
            .tag("operation", operation)
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry)
            .record(duration, unit);
    }

    /**
     * Record a database operation
     */
    public void recordDatabaseOperation(String operation, String table, long duration) {
        meterRegistry.timer(
            APP_NAMESPACE + "_database_operation_duration",
            "operation", operation,
            "table", table
        ).record(duration, TimeUnit.MILLISECONDS);
    }

    /**
     * Count API errors by type
     */
    public void recordApiError(String endpoint, String errorType, int statusCode) {
        meterRegistry.counter(
            APP_NAMESPACE + "_api_errors_total",
            "endpoint", endpoint,
            "errorType", errorType,
            "status", String.valueOf(statusCode)
        ).increment();
    }

    /**
     * Record cache hits/misses
     */
    public void recordCacheHit(String cacheName) {
        meterRegistry.counter(
            APP_NAMESPACE + "_cache_hits_total",
            "cache", cacheName
        ).increment();
    }

    public void recordCacheMiss(String cacheName) {
        meterRegistry.counter(
            APP_NAMESPACE + "_cache_misses_total",
            "cache", cacheName
        ).increment();
    }

    /**
     * Gauge for active operations
     */
    public void setActiveOperations(String operation, int count) {
        meterRegistry.gauge(
            APP_NAMESPACE + "_active_operations",
            count,
            measurement -> measurement.tag("operation", operation)
        );
    }
}
