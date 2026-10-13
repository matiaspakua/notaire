package com.licensis.notaire.adapter.out.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportTheme print tokens (issue #1593)")
class ReportThemeTest {

    @Test
    @DisplayName("shouldExposeA4PortraitMarginsAndEsArLocale")
    void shouldExposeA4PortraitMarginsAndEsArLocale() {
        assertThat(ReportTheme.LOCALE).isEqualTo(Locale.forLanguageTag("es-AR"));
        assertThat(ReportTheme.LANGUAGE).isEqualTo("es-AR");
        assertThat(ReportTheme.ZONE.getId()).isEqualTo("America/Argentina/Buenos_Aires");
        assertThat(ReportTheme.pageWidthPt(false)).isEqualTo(210f * ReportTheme.MM_TO_PT, org.assertj.core.data.Offset.offset(0.5f));
        assertThat(ReportTheme.pageHeightPt(false)).isEqualTo(297f * ReportTheme.MM_TO_PT, org.assertj.core.data.Offset.offset(0.5f));
        assertThat(ReportTheme.MARGIN_TOP_PT).isEqualTo(18f * ReportTheme.MM_TO_PT, org.assertj.core.data.Offset.offset(0.2f));
        assertThat(ReportTheme.MARGIN_BOTTOM_PT).isEqualTo(16f * ReportTheme.MM_TO_PT, org.assertj.core.data.Offset.offset(0.2f));
        assertThat(ReportTheme.TITLE_SIZE_PT).isEqualTo(18f);
        assertThat(ReportTheme.BODY_SIZE_PT).isEqualTo(10f);
        assertThat(ReportTheme.BODY_LEADING_PT).isEqualTo(13f);
        assertThat(ReportTheme.WORDMARK).isEqualTo("Notaire");
    }
}
