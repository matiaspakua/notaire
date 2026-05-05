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
            // Check application readiness
            boolean isReady = checkApplicationReadiness();
            
            if (isReady) {
                structuredLogger.logHealthProbe(
                    "application_health",
                    "UP",
                    Map.of("timestamp", System.currentTimeMillis())
                );
                return Health.up()
                    .withDetail("service", "notaire-backend")
                    .withDetail("status", "operational")
                    .build();
            } else {
                structuredLogger.logHealthProbe(
                    "application_health",
                    "DOWN",
                    Map.of("reason", "application not ready")
                );
                return Health.down()
                    .withDetail("service", "notaire-backend")
                    .withDetail("reason", "Application not fully initialized")
                    .build();
            }
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

    /**
     * Check if the application is ready to serve requests
     */
    private boolean checkApplicationReadiness() {
        // Add custom readiness checks here
        // For now, always return true if we reach here (database already checked by DB health indicator)
        return true;
    }
}
