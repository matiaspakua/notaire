package com.licensis.notaire.application.usecase.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;

@DisplayName("TOTP verification use case (CU78, ADR-032)")
class TotpVerificationServiceTest {

    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);

    private MfaFakes.InMemoryStore store;
    private MfaFakes.FixedClock clock;
    private TotpVerificationService verification;
    private byte[] secret;

    @BeforeEach
    void setUp() {
        store = new MfaFakes.InMemoryStore();
        clock = new MfaFakes.FixedClock(NOW);
        TotpEnrollmentService enrolment = new TotpEnrollmentService(store, new MfaFakes.CountingRandom(), clock,
                new MfaFakes.ReversingHasher());
        verification = new TotpVerificationService(store, clock);
        TotpEnrollmentStart start = enrolment.begin(7, "admin");
        secret = Base32Codec.decode(start.secret());
        enrolment.confirm(7, TotpAlgorithm.code(secret, TotpAlgorithm.stepAt(NOW)));
        clock.now = NOW.plusSeconds(60);
    }

    private String codeAt(Instant at) {
        return TotpAlgorithm.code(secret, TotpAlgorithm.stepAt(at));
    }

    @Test
    @DisplayName("accepts the current code")
    void shouldAcceptCurrentCode() {
        assertThat(verification.verify(7, codeAt(clock.now))).isTrue();
    }

    @Test
    @DisplayName("rejects a wrong code")
    void shouldRejectWrongCode() {
        String current = codeAt(clock.now);
        String wrong = "000000".equals(current) ? "111111" : "000000";

        assertThat(verification.verify(7, wrong)).isFalse();
    }

    @Test
    @DisplayName("rejects the same code twice (replay)")
    void shouldRejectReplay() {
        String code = codeAt(clock.now);

        assertThat(verification.verify(7, code)).isTrue();
        assertThat(verification.verify(7, code)).isFalse();
    }

    @Test
    @DisplayName("rejects when the user has no enrolment")
    void shouldRejectUnknownUser() {
        assertThat(verification.verify(99, "123456")).isFalse();
    }

    @Test
    @DisplayName("rejects when the enrolment is not confirmed")
    void shouldRejectUnconfirmedEnrolment() {
        MfaFakes.InMemoryStore fresh = new MfaFakes.InMemoryStore();
        TotpEnrollmentService enrolment = new TotpEnrollmentService(fresh, new MfaFakes.CountingRandom(), clock,
                new MfaFakes.ReversingHasher());
        TotpEnrollmentStart start = enrolment.begin(8, "notary");
        String code = TotpAlgorithm.code(Base32Codec.decode(start.secret()), TotpAlgorithm.stepAt(clock.now));

        assertThat(new TotpVerificationService(fresh, clock).verify(8, code)).isFalse();
    }
}
