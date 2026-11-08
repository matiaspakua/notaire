package com.licensis.notaire.application.usecase.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.MfaAlreadyEnrolledException;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;

@DisplayName("TOTP enrolment use case (CU78, ADR-032)")
class TotpEnrollmentServiceTest {

    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);

    private MfaFakes.InMemoryStore store;
    private TotpEnrollmentService service;

    @BeforeEach
    void setUp() {
        store = new MfaFakes.InMemoryStore();
        service = new TotpEnrollmentService(store, new MfaFakes.CountingRandom(),
                new MfaFakes.FixedClock(NOW), new MfaFakes.ReversingHasher());
    }

    private String currentCode(String base32Secret) {
        return TotpAlgorithm.code(Base32Codec.decode(base32Secret), TotpAlgorithm.stepAt(NOW));
    }

    @Test
    @DisplayName("begin returns a 160-bit secret and an otpauth URI, unconfirmed")
    void shouldBeginEnrolment() {
        TotpEnrollmentStart start = service.begin(7, "admin");

        assertThat(Base32Codec.decode(start.secret())).hasSize(20);
        assertThat(start.otpauthUri()).startsWith("otpauth://totp/Notaire:admin?secret=" + start.secret())
                .contains("issuer=Notaire", "digits=6", "period=30");
        assertThat(store.rows.get(7).confirmed()).isFalse();
    }

    @Test
    @DisplayName("confirm with a valid code returns 10 recovery codes and confirms")
    void shouldConfirmWithValidCode() {
        TotpEnrollmentStart start = service.begin(7, "admin");

        List<String> codes = service.confirm(7, currentCode(start.secret()));

        assertThat(codes).hasSize(10).doesNotHaveDuplicates().allSatisfy(c -> assertThat(c).hasSize(10));
        assertThat(store.rows.get(7).confirmed()).isTrue();
        assertThat(store.rows.get(7).recoveryCodeHashes()).hasSize(10).doesNotContainAnyElementsOf(codes);
    }

    @Test
    @DisplayName("confirm with a wrong code fails and issues nothing")
    void shouldRejectWrongCode() {
        TotpEnrollmentStart start = service.begin(7, "admin");
        String wrong = "000000".equals(currentCode(start.secret())) ? "111111" : "000000";

        assertThatThrownBy(() -> service.confirm(7, wrong)).isInstanceOf(InvalidMfaCodeException.class);
        assertThat(store.rows.get(7).confirmed()).isFalse();
        assertThat(store.rows.get(7).recoveryCodeHashes()).isEmpty();
    }

    @Test
    @DisplayName("confirm without begin fails")
    void shouldRejectConfirmWithoutBegin() {
        assertThatThrownBy(() -> service.confirm(7, "123456")).isInstanceOf(MfaNotEnrolledException.class);
    }

    @Test
    @DisplayName("begin on an already confirmed user is rejected")
    void shouldRejectSecondEnrolment() {
        TotpEnrollmentStart start = service.begin(7, "admin");
        service.confirm(7, currentCode(start.secret()));

        assertThatThrownBy(() -> service.begin(7, "admin")).isInstanceOf(MfaAlreadyEnrolledException.class);
    }

    @Test
    @DisplayName("begin on an unconfirmed user replaces the pending secret")
    void shouldReplacePendingEnrolment() {
        TotpEnrollmentStart first = service.begin(7, "admin");
        TotpEnrollmentStart second = service.begin(7, "admin");

        assertThat(second.secret()).isNotEqualTo(first.secret());
    }
}
