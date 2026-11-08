package com.licensis.notaire.application.usecase.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;

@DisplayName("Recovery code use case (CU78, ADR-032)")
class RecoveryCodeServiceTest {

    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);

    private MfaFakes.InMemoryStore store;
    private RecoveryCodeService service;
    private List<String> codes;

    @BeforeEach
    void setUp() {
        store = new MfaFakes.InMemoryStore();
        MfaFakes.ReversingHasher hasher = new MfaFakes.ReversingHasher();
        TotpEnrollmentService enrolment = new TotpEnrollmentService(store, new MfaFakes.CountingRandom(),
                new MfaFakes.FixedClock(NOW), hasher);
        service = new RecoveryCodeService(store, hasher);
        TotpEnrollmentStart start = enrolment.begin(7, "admin");
        codes = enrolment.confirm(7, TotpAlgorithm.code(Base32Codec.decode(start.secret()), TotpAlgorithm.stepAt(NOW)));
    }

    @Test
    @DisplayName("a valid recovery code works once")
    void shouldAcceptOnce() {
        assertThat(service.use(7, codes.get(0))).isTrue();
        assertThat(service.use(7, codes.get(0))).isFalse();
        assertThat(store.rows.get(7).recoveryCodeHashes()).hasSize(9);
    }

    @Test
    @DisplayName("is case and separator insensitive")
    void shouldNormaliseInput() {
        String lower = " " + codes.get(1).toLowerCase() + " ";

        assertThat(service.use(7, lower)).isTrue();
    }

    @Test
    @DisplayName("rejects an unknown code")
    void shouldRejectUnknownCode() {
        assertThat(service.use(7, "AAAAAAAAAA")).isFalse();
    }

    @Test
    @DisplayName("rejects when the user has no confirmed enrolment")
    void shouldRejectWithoutEnrolment() {
        assertThat(service.use(99, codes.get(0))).isFalse();
    }
}
