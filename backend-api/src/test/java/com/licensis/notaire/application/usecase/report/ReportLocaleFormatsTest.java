package com.licensis.notaire.application.usecase.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportLocaleFormats es-AR helpers (issue #1593)")
class ReportLocaleFormatsTest {

    @Test
    @DisplayName("shouldFormatArgentineMoneyAndDates")
    void shouldFormatArgentineMoneyAndDates() {
        assertThat(ReportLocaleFormats.money(new BigDecimal("1234.56"))).isEqualTo("$ 1.234,56");
        assertThat(ReportLocaleFormats.money(new BigDecimal("-10.5"))).isEqualTo("$ -10,50");
        assertThat(ReportLocaleFormats.money(null)).isEqualTo("N/A");
        assertThat(ReportLocaleFormats.day(LocalDate.of(2026, 3, 15))).isEqualTo("15/03/2026");
        assertThat(ReportLocaleFormats.day(java.sql.Date.valueOf(LocalDate.of(2026, 3, 15))))
                .isEqualTo("15/03/2026");
        assertThat(ReportLocaleFormats.day((java.util.Date) null)).isEqualTo("N/A");
        assertThat(ReportLocaleFormats.dayMonth(java.sql.Date.valueOf(LocalDate.of(2026, 6, 2))))
                .isEqualTo("02/06");
        assertThat(ReportLocaleFormats.dayMonth((java.util.Date) null)).isEqualTo("N/A");
        assertThat(ReportLocaleFormats.fallback(null)).isEqualTo("N/A");
        assertThat(ReportLocaleFormats.fallback("  ")).isEqualTo("N/A");
        assertThat(ReportLocaleFormats.generatedAt(
                Instant.parse("2026-03-15T18:30:00Z"),
                ZoneId.of("America/Argentina/Buenos_Aires"))).contains("15/03/2026 15:30");
    }
}
