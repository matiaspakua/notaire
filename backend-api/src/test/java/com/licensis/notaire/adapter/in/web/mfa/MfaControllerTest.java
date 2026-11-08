package com.licensis.notaire.adapter.in.web.mfa;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.application.port.in.mfa.DisableTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.EnrollTotpUseCase;
import com.licensis.notaire.application.port.in.mfa.TotpEnrollmentStart;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.domain.mfa.InvalidMfaCodeException;
import com.licensis.notaire.domain.mfa.MfaAlreadyEnrolledException;
import com.licensis.notaire.domain.mfa.MfaNotEnrolledException;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.LoginAttemptService;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU78"})
@ExtendWith(MockitoExtension.class)
@DisplayName("MfaController (CU78, ADR-032)")
class MfaControllerTest {

    private static final User JUAN = User.restore(4, "juan", "hash", true, "ADMIN", null, null, null, null, null, 0);
    private static final Principal PRINCIPAL = () -> "juan";
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Mock private QueryUsersUseCase users;
    @Mock private EnrollTotpUseCase enrol;
    @Mock private DisableTotpUseCase disable;
    @Mock private LoginAttemptService attempts;
    @Mock private MetricsUtil metrics;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new MfaController(users, enrol, disable, attempts, metrics))
                .setControllerAdvice(new MfaExceptionHandler(), new GlobalExceptionHandler())
                .build();
        lenient().when(users.findByName("juan")).thenReturn(Optional.of(JUAN));
    }

    @Test
    @DisplayName("begin returns the secret and URI once, uncached, for the caller's own user id")
    void shouldBeginEnrolment() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        when(enrol.begin(4, "juan")).thenReturn(new TotpEnrollmentStart("SECRET", "otpauth://totp/x"));

        mvc.perform(post("/api/v1/mfa/enrollment").principal(PRINCIPAL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret").value("SECRET"))
                .andExpect(jsonPath("$.otpauthUri").value("otpauth://totp/x"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    @DisplayName("begin answers 409 when already enrolled")
    void shouldRejectBeginWhenEnrolled() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        when(enrol.begin(4, "juan")).thenThrow(new MfaAlreadyEnrolledException());

        mvc.perform(post("/api/v1/mfa/enrollment").principal(PRINCIPAL)).andExpect(status().isConflict());
    }

    @Test
    @DisplayName("confirm returns the recovery codes once")
    void shouldConfirmEnrolment() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        when(enrol.confirm(4, "123456")).thenReturn(List.of("AAAAABBBBB", "CCCCCDDDDD"));

        mvc.perform(post("/api/v1/mfa/enrollment/confirm").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recoveryCodes[0]").value("AAAAABBBBB"))
                .andExpect(header().string("Cache-Control", org.hamcrest.Matchers.containsString("no-store")));
    }

    @Test
    @DisplayName("a wrong confirmation code answers 400 and counts toward lockout")
    void shouldCountWrongConfirmCode() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        when(enrol.confirm(4, "000000")).thenThrow(new InvalidMfaCodeException());

        mvc.perform(post("/api/v1/mfa/enrollment/confirm").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest());

        verify(attempts).onLoginFailed("juan");
        verify(metrics).incrementCounter("login", "mfa_failed");
    }

    @Test
    @DisplayName("confirm without a begun enrolment answers 404")
    void shouldReportMissingEnrolment() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        when(enrol.confirm(4, "123456")).thenThrow(new MfaNotEnrolledException());

        mvc.perform(post("/api/v1/mfa/enrollment/confirm").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("a locked account is refused with 429 before any MFA work")
    void shouldRefuseWhenLocked() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(true);

        mvc.perform(post("/api/v1/mfa/enrollment/confirm").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isTooManyRequests());

        verify(enrol, never()).confirm(4, "123456");
    }

    @Test
    @DisplayName("disable removes the enrolment with a current code")
    void shouldDisable() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);

        mvc.perform(post("/api/v1/mfa/disable").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isNoContent());

        verify(disable).disable(4, "123456", null);
    }

    @Test
    @DisplayName("disable with a wrong code answers 400 and counts toward lockout")
    void shouldCountWrongDisableCode() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);
        org.mockito.Mockito.doThrow(new InvalidMfaCodeException()).when(disable).disable(4, "000000", null);

        mvc.perform(post("/api/v1/mfa/disable").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"code\":\"000000\"}"))
                .andExpect(status().isBadRequest());

        verify(attempts).onLoginFailed("juan");
        verify(attempts, never()).onLoginSucceeded(anyString());
    }

    @Test
    @DisplayName("disable accepts a recovery code")
    void shouldDisableWithRecoveryCode() throws Exception {
        when(attempts.isLocked("juan")).thenReturn(false);

        mvc.perform(post("/api/v1/mfa/disable").principal(PRINCIPAL)
                        .contentType(JSON).content("{\"recoveryCode\":\"ABCDEFGHJK\"}"))
                .andExpect(status().isNoContent());

        verify(disable).disable(4, null, "ABCDEFGHJK");
    }
}
