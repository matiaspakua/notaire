package com.licensis.notaire.adapter.out.pdf;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportGenerationRecorder (issue #1593)")
class ReportGenerationRecorderTest {

    @Test
    @DisplayName("shouldRecordTypeOutcomePagesAndRowsWithoutPayload")
    void shouldRecordTypeOutcomePagesAndRowsWithoutPayload() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ReportGenerationRecorder recorder = new ReportGenerationRecorder(registry);

        recorder.recordSuccess("CU01 Lucía", 1_000_000L, 2, 12);
        recorder.recordFailure("CU01 Lucía");

        assertThat(registry.find("report_generation_total").tag("type", "CU01_Luc_a").tag("outcome", "success")
                .counter()).isNotNull();
        assertThat(registry.find("report_pages").summary().count()).isEqualTo(1);
        assertThat(registry.find("report_rows").summary().totalAmount()).isEqualTo(12);
        assertThat(registry.getMeters().stream().map(meter -> meter.getId().toString()).toList())
                .noneMatch(id -> id.contains("Peña") || id.contains("deed"));
    }
}
