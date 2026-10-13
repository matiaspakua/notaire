package com.licensis.notaire.adapter.out.pdf;

import java.time.ZoneId;
import java.util.Locale;

/**
 * Print tokens for the PDF adapter only. Application and domain code must not
 * depend on this type (issue #1593).
 */
public final class ReportTheme {

    public static final Locale LOCALE = Locale.forLanguageTag("es-AR");
    public static final String LANGUAGE = "es-AR";
    public static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    /** PDF point per millimetre. */
    public static final float MM_TO_PT = 72f / 25.4f;

    public static final float MARGIN_TOP_PT = 18f * MM_TO_PT;
    public static final float MARGIN_SIDE_PT = 18f * MM_TO_PT;
    public static final float MARGIN_BOTTOM_PT = 16f * MM_TO_PT;
    public static final float GRID_PT = 4f * MM_TO_PT;
    public static final float SECTION_GAP_PT = 8f * MM_TO_PT;
    public static final float TITLE_META_GAP_PT = 4f * MM_TO_PT;
    public static final float CELL_PADDING_PT = 2.5f * MM_TO_PT;
    public static final float HEADER_BAND_PT = 12f * MM_TO_PT;
    public static final float FOOTER_BAND_PT = 10f * MM_TO_PT;

    public static final float TITLE_SIZE_PT = 18f;
    public static final float HEADING_SIZE_PT = 12f;
    public static final float BODY_SIZE_PT = 10f;
    public static final float BODY_LEADING_PT = 13f;
    public static final float META_SIZE_PT = 9f;
    public static final float TABLE_SIZE_PT = 9f;
    public static final float FOOTER_SIZE_PT = 8f;
    public static final float WORDMARK_SIZE_PT = 14f;

    /** Near-black text; contrast on white exceeds 4.5:1. */
    public static final float TEXT_GRAY = 0.10f;
    /** Brand accent used only for rules and the wordmark; still dark in grayscale. */
    public static final float ACCENT_R = 0.48f;
    public static final float ACCENT_G = 0.12f;
    public static final float ACCENT_B = 0.12f;
    public static final float MUTED_GRAY = 0.35f;
    public static final float HEADER_FILL = 0.90f;
    public static final float RULE_GRAY = 0.70f;

    public static final String WORDMARK = "Notaire";
    public static final String WORDMARK_ALT = "Notaire";
    public static final String PAGE_LABEL_FORMAT = "Página %d de %d";
    public static final String NULL_FALLBACK = "N/A";

    private ReportTheme() {
    }

    public static float pageWidthPt(boolean landscape) {
        return (landscape ? 297f : 210f) * MM_TO_PT;
    }

    public static float pageHeightPt(boolean landscape) {
        return (landscape ? 210f : 297f) * MM_TO_PT;
    }
}
