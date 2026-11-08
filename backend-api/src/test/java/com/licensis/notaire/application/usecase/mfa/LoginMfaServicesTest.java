package com.licensis.notaire.application.usecase.mfa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.domain.mfa.Base32Codec;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.LoginMfaDecision;
import com.licensis.notaire.domain.mfa.LoginMfaPolicy;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;
import com.licensis.notaire.domain.mfa.MfaPolicyMode;
import com.licensis.notaire.domain.mfa.TotpAlgorithm;
import com.licensis.notaire.domain.user.AccountRole;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU78"})
@DisplayName("Login MFA use cases (CU78, ADR-032)")
class LoginMfaServicesTest {

    private static final Instant NOW = Instant.ofEpochSecond(1_700_000_000L);
    private static final int ADMIN_ID = 7;

    private MfaFakes.InMemoryStore store;
    private MfaFakes.FixedClock clock;
    private TotpEnrollmentService enrolment;
    private TotpVerificationService verification;
    private RecoveryCodeService recovery;
    private SecondFactorService secondFactor;
    private byte[] secret;
    private List<String> recoveryCodes;

    @BeforeEach
    void setUp() {
        store = new MfaFakes.InMemoryStore();
        clock = new MfaFakes.FixedClock(NOW);
        MfaFakes.ReversingHasher hasher = new MfaFakes.ReversingHasher();
        enrolment = new TotpEnrollmentService(store, new MfaFakes.CountingRandom(), clock, hasher);
        verification = new TotpVerificationService(store, clock);
        recovery = new RecoveryCodeService(store, hasher);
        secondFactor = new SecondFactorService(verification, recovery);
    }

    private void enrol() {
        TotpEnrollmentStart start = enrolment.begin(ADMIN_ID, "admin");
        secret = Base32Codec.decode(start.secret());
        recoveryCodes = enrolment.confirm(ADMIN_ID, currentCode());
        clock.now = NOW.plusSeconds(60);
    }

    private String currentCode() {
        return TotpAlgorithm.code(secret, TotpAlgorithm.stepAt(clock.now));
    }

    private AssessLoginMfaService assessing(MfaPolicyMode mode) {
        return new AssessLoginMfaService(store,
                new LoginMfaPolicy(mode, Set.of(AccountRole.ADMIN, AccountRole.NOTARY)));
    }

    @Test
    @DisplayName("assess: confirmed enrolment yields a challenge")
    void shouldChallengeConfirmedEnrolment() {
        enrol();
        assertThat(assessing(MfaPolicyMode.OPTIONAL).assess(ADMIN_ID, "ADMIN")).isEqualTo(LoginMfaDecision.CHALLENGE);
    }

    @Test
    @DisplayName("assess: an unconfirmed enrolment never gates a login")
    void shouldIgnoreUnconfirmedEnrolment() {
        enrolment.begin(ADMIN_ID, "admin");
        assertThat(assessing(MfaPolicyMode.OPTIONAL).assess(ADMIN_ID, "ADMIN")).isEqualTo(LoginMfaDecision.SESSION);
    }

    @Test
    @DisplayName("assess: enforcement asks an admin without enrolment to enrol")
    void shouldRequireEnrolmentUnderEnforce() {
        assertThat(assessing(MfaPolicyMode.ENFORCE).assess(ADMIN_ID, "ADMIN"))
                .isEqualTo(LoginMfaDecision.ENROLLMENT_REQUIRED);
        assertThat(assessing(MfaPolicyMode.ENFORCE).assess(ADMIN_ID, "Secretario")).isEqualTo(LoginMfaDecision.SESSION);
    }

    @Test
    @DisplayName("second factor: accepts a TOTP, a recovery code once, and rejects the rest")
    void shouldVerifySecondFactor() {
        enrol();
        assertThat(secondFactor.verify(ADMIN_ID, "000000", null)).isFalse();
        assertThat(secondFactor.verify(ADMIN_ID, currentCode(), null)).isTrue();
        assertThat(secondFactor.verify(ADMIN_ID, null, recoveryCodes.get(0))).isTrue();
        assertThat(secondFactor.verify(ADMIN_ID, null, recoveryCodes.get(0))).isFalse();
        assertThat(secondFactor.verify(ADMIN_ID, null, null)).isFalse();
        assertThat(secondFactor.verify(ADMIN_ID, " ", " ")).isFalse();
    }

    @Test
    @DisplayName("second factor: a recovery code takes precedence over a code field")
    void shouldPreferRecoveryWhenGiven() {
        enrol();
        assertThat(secondFactor.verify(ADMIN_ID, "000000", recoveryCodes.get(1))).isTrue();
    }

    @Test
    @DisplayName("disable: removes the enrolment with a current code, keeps it with a wrong one")
    void shouldDisableWithCurrentCode() {
        enrol();
        TotpDisableService disable = new TotpDisableService(store, secondFactor);
        assertThatThrownBy(() -> disable.disable(ADMIN_ID, "000000", null)).isInstanceOf(InvalidMfaCodeException.class);
        assertThat(store.rows).containsKey(ADMIN_ID);
        disable.disable(ADMIN_ID, currentCode(), null);
        assertThat(store.rows).doesNotContainKey(ADMIN_ID);
    }

    @Test
    @DisplayName("disable: fails when nothing is confirmed")
    void shouldFailDisableWithoutEnrolment() {
        TotpDisableService disable = new TotpDisableService(store, secondFactor);
        assertThatThrownBy(() -> disable.disable(ADMIN_ID, "123456", null)).isInstanceOf(MfaNotEnrolledException.class);
    }
}
