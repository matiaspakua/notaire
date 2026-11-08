package com.licensis.notaire.observability;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

/**
 * Custom health indicator for application-specific health checks
 * Reports to /actuator/health endpoint
 */
@Component
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationHealthIndicator.class);
    private static final StructuredLogger structuredLogger = StructuredLogger.getInstance();

    @Override
    public Health health() {
        try {
            // Application process is up; database readiness is a separate health indicator.
            structuredLogger.logHealthProbe(
                "application_health",
                "UP",
                Map.of("timestamp", System.currentTimeMillis())
            );
            return Health.up()
                .withDetail("service", "notaire-backend")
                .withDetail("status", "operational")
                .build();
        } catch (Exception e) {
            logger.error("Health check failed", e);
            structuredLogger.logError(
                "health_check_failed",
                e,
                Map.of("component", "application_health")
            );
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
