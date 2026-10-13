package com.licensis.notaire.adapter.out.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReportTextSanitizer (issue #1593)")
class ReportTextSanitizerTest {

    @Test
    @DisplayName("shouldNeutralizeControlsBidiAndCapLength")
    void shouldNeutralizeControlsBidiAndCapLength() {
        assertThat(ReportTextSanitizer.neutralize(null)).isEmpty();
        assertThat(ReportTextSanitizer.neutralize("a\nb\tc\u202Ed")).isEqualTo("a b c d");
        String huge = "x".repeat(ReportTextSanitizer.MAX_CHARS + 50);
        assertThat(ReportTextSanitizer.neutralize(huge)).hasSize(ReportTextSanitizer.MAX_CHARS);
    }
}
