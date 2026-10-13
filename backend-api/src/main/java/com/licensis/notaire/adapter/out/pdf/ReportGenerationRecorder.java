package com.licensis.notaire.adapter.out.pdf;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Report generation metrics. Tags are report type and outcome only — never names,
 * identification numbers, or document content (issue #1593).
 */
@Component
public class ReportGenerationRecorder {

    private final MeterRegistry meterRegistry;

    public ReportGenerationRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordSuccess(String reportType, long durationNanos, int pages, int rows) {
        String type = safeType(reportType);
        meterRegistry.counter("report_generation_total", "type", type, "outcome", "success").increment();
        meterRegistry.timer("report_generation_seconds").record(durationNanos, TimeUnit.NANOSECONDS);
        meterRegistry.summary("report_pages").record(pages);
        meterRegistry.summary("report_rows").record(rows);
    }

    public void recordFailure(String reportType) {
        meterRegistry.counter("report_generation_total", "type", safeType(reportType), "outcome", "error")
                .increment();
    }

    private static String safeType(String reportType) {
        if (reportType == null || reportType.isBlank()) {
            return "unknown";
        }
        return reportType.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}
