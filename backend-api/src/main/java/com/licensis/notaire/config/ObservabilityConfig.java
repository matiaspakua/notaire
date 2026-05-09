package com.licensis.notaire.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Observability configuration for Micrometer metrics.
 * Registers JVM, system, and application-specific metrics
 * for Prometheus scraping via the /actuator/prometheus endpoint.
 */
@Configuration
public class ObservabilityConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ObservabilityConfig.class);

    private final MeterRegistry meterRegistry;

    public ObservabilityConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        LOG.info("Initializing observability metrics for Notaire Backend");
    }

    /**
     * JVM Memory metrics - heap, non-heap, buffer pools
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics")
    public JvmMemoryMetrics jvmMemoryMetrics() {
        JvmMemoryMetrics metrics = new JvmMemoryMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("JVM Memory metrics registered");
        return metrics;
    }

    /**
     * JVM GC metrics - garbage collection pauses and counts
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.jvm.JvmGcMetrics")
    public JvmGcMetrics jvmGcMetrics() {
        JvmGcMetrics metrics = new JvmGcMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("JVM GC metrics registered");
        return metrics;
    }

    /**
     * JVM Thread metrics - live, daemon, peak thread counts
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics")
    public JvmThreadMetrics jvmThreadMetrics() {
        JvmThreadMetrics metrics = new JvmThreadMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("JVM Thread metrics registered");
        return metrics;
    }

    /**
     * Processor metrics - CPU usage, load average
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.system.ProcessorMetrics")
    public ProcessorMetrics processorMetrics() {
        ProcessorMetrics metrics = new ProcessorMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("Processor metrics registered");
        return metrics;
    }

    /**
     * Uptime metrics - application uptime tracking
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.system.UptimeMetrics")
    public UptimeMetrics uptimeMetrics() {
        UptimeMetrics metrics = new UptimeMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("Uptime metrics registered");
        return metrics;
    }

    /**
     * Logback metrics - log event counting by level
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.logging.LogbackMetrics")
    public LogbackMetrics logbackMetrics() {
        LogbackMetrics metrics = new LogbackMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("Logback metrics registered");
        return metrics;
    }

    /**
     * ClassLoader metrics - loaded/unloaded classes
     */
    @Bean
    @ConditionalOnClass(name = "io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics")
    public ClassLoaderMetrics classLoaderMetrics() {
        ClassLoaderMetrics metrics = new ClassLoaderMetrics();
        metrics.bindTo(meterRegistry);
        LOG.debug("ClassLoader metrics registered");
        return metrics;
    }

    /**
     * Register a custom Gauge for notaire-shared module version
     * This exposes the shared module version as a metric for tracking
     */
    @Bean
    public Gauge notaireSharedVersion() {
        return Gauge.builder("notaire_shared_version", () -> {
                try {
                    String version = getClass().getPackage().getImplementationVersion();
                    return version != null ? Double.parseDouble(version.replaceAll("[^0-9.]", "").substring(0, 1)) : 1.0;
                } catch (Exception e) {
                    return 1.0;
                }
            })
            .description("Notaire shared module version indicator")
            .tag("module", "notaire-shared")
            .register(meterRegistry);
    }
}
