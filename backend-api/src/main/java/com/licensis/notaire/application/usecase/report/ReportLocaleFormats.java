package com.licensis.notaire.application.usecase.report;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Deterministic {@code es-AR} money and date formatting shared by report builders.
 */
public final class ReportLocaleFormats {

    public static final Locale ES_AR = Locale.of("es", "AR");
    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    public static final String NOT_RECORDED = "N/A";

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GENERATED = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportLocaleFormats() {
    }

    public static String money(BigDecimal amount) {
        if (amount == null) {
            return NOT_RECORDED;
        }
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ES_AR));
        return "$ " + format.format(amount);
    }

    public static String day(LocalDate date) {
        return date == null ? NOT_RECORDED : DAY.format(date);
    }

    public static String fallback(String value) {
        return value == null || value.isBlank() ? NOT_RECORDED : value;
    }

    public static String generatedAt(Instant instant, ZoneId zone) {
        ZoneId resolved = zone == null ? ZONE : zone;
        Instant when = instant == null ? Instant.EPOCH : instant;
        return GENERATED.format(when.atZone(resolved));
    }
}
