package com.licensis.notaire.domain.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.OptionalLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TOTP algorithm — RFC 6238 (CU78, ADR-032)")
class TotpAlgorithmTest {

    private static final byte[] RFC_SECRET = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

    @Test
    @DisplayName("matches the RFC 6238 SHA-1 vector at t=59")
    void shouldMatchRfcVectorAt59() {
        long step = TotpAlgorithm.stepAt(Instant.ofEpochSecond(59));

        assertThat(TotpAlgorithm.code(RFC_SECRET, step)).isEqualTo("287082");
    }

    @Test
    @DisplayName("matches the RFC 6238 SHA-1 vector at t=1111111109")
    void shouldMatchRfcVectorAt1111111109() {
        long step = TotpAlgorithm.stepAt(Instant.ofEpochSecond(1111111109L));

        assertThat(TotpAlgorithm.code(RFC_SECRET, step)).isEqualTo("081804");
    }

    @Test
    @DisplayName("accepts the previous step to tolerate clock skew")
    void shouldAcceptPreviousStep() {
        long now = TotpAlgorithm.stepAt(Instant.ofEpochSecond(1111111109L));
        String previous = TotpAlgorithm.code(RFC_SECRET, now - 1);

        assertThat(TotpAlgorithm.matchingStep(RFC_SECRET, previous, now)).hasValue(now - 1);
    }

    @Test
    @DisplayName("rejects a code two steps away")
    void shouldRejectCodeOutsideWindow() {
        long now = TotpAlgorithm.stepAt(Instant.ofEpochSecond(1111111109L));
        String stale = TotpAlgorithm.code(RFC_SECRET, now - 2);

        assertThat(TotpAlgorithm.matchingStep(RFC_SECRET, stale, now)).isEqualTo(OptionalLong.empty());
    }

    @Test
    @DisplayName("rejects malformed codes")
    void shouldRejectMalformedCodes() {
        assertThat(TotpAlgorithm.matchingStep(RFC_SECRET, "12ab56", 10)).isEmpty();
        assertThat(TotpAlgorithm.matchingStep(RFC_SECRET, "12345", 10)).isEmpty();
        assertThat(TotpAlgorithm.matchingStep(RFC_SECRET, null, 10)).isEmpty();
    }
}
