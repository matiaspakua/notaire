package com.licensis.notaire.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized structured logging utility for observability
 * Supports JSON logging for ELK/Prometheus integration
 * Implements singleton pattern for consistency
 */
public final class StructuredLogger {
    private static final StructuredLogger INSTANCE = new StructuredLogger();
    private static final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private StructuredLogger() {
    }

    public static StructuredLogger getInstance() {
        return INSTANCE;
    }

    /**
     * Log at TRACE level with structured context
     */
    public void logTrace(String message, Map<String, Object> context) {
        if (logger.isTraceEnabled()) {
            Map<String, Object> logData = buildLogData("TRACE", message, context);
            logger.trace(toJson(logData));
        }
    }

    /**
     * Log at DEBUG level with structured context
     */
    public void logDebug(String message, Map<String, Object> context) {
        if (logger.isDebugEnabled()) {
            Map<String, Object> logData = buildLogData("DEBUG", message, context);
            logger.debug(toJson(logData));
        }
    }

    /**
     * Log at INFO level (events and metrics)
     */
    public void logInfo(String message, Map<String, Object> context) {
        Map<String, Object> logData = buildLogData("INFO", message, context);
        logger.info(toJson(logData));
    }

    /**
     * Log at WARN level (warnings and recoverable errors)
     */
    public void logWarn(String message, Map<String, Object> context) {
        Map<String, Object> logData = buildLogData("WARN", message, context);
        logger.warn(toJson(logData));
    }

    /**
     * Log at ERROR level with exception
     */
    public void logError(String message, Throwable throwable, Map<String, Object> context) {
        Map<String, Object> logData = buildLogData("ERROR", message, context);
        logData.put("exception", throwable.getClass().getSimpleName());
        logData.put("exceptionMessage", throwable.getMessage());
        logData.put("stackTrace", getStackTrace(throwable));
        logger.error(toJson(logData), throwable);
    }

    /**
     * Log business metric event
     */
    public void logMetric(String metricName, double value, Map<String, Object> tags) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("type", "METRIC");
        logData.put("metric", metricName);
        logData.put("value", value);
        logData.putAll(tags);
        logger.info(toJson(logData));
    }

    /**
     * Log audit event (security/compliance)
     */
    public void logAudit(String action, String actor, String resource, Map<String, Object> details) {
        Map<String, Object> logData = buildLogData("AUDIT", action, Map.of(
            "actor", actor,
            "resource", resource
        ));
        logData.putAll(details);
        logger.info(toJson(logData));
    }

    /**
     * Log health probe result
     */
    public void logHealthProbe(String probeName, String status, Map<String, Object> details) {
        Map<String, Object> logData = buildLogData("PROBE", probeName, Map.of(
            "status", status
        ));
        logData.putAll(details);
        logger.info(toJson(logData));
    }

    /**
     * Build base log data structure
     */
    private Map<String, Object> buildLogData(String level, String message, Map<String, Object> context) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", System.currentTimeMillis());
        logData.put("level", level);
        logData.put("message", message);
        logData.put("thread", Thread.currentThread().getName());
        if (context != null) {
            logData.putAll(context);
        }
        return logData;
    }

    /**
     * Convert log data to JSON string
     */
    private String toJson(Map<String, Object> logData) {
        try {
            return objectMapper.writeValueAsString(logData);
        } catch (Exception e) {
            // Fallback to string representation if JSON serialization fails
            return logData.toString();
        }
    }

    /**
     * Extract stack trace as array
     */
    private String[] getStackTrace(Throwable throwable) {
        StackTraceElement[] elements = throwable.getStackTrace();
        String[] trace = new String[Math.min(elements.length, 10)]; // Limit to first 10 stack frames
        for (int i = 0; i < trace.length; i++) {
            trace[i] = elements[i].toString();
        }
        return trace;
    }
}
