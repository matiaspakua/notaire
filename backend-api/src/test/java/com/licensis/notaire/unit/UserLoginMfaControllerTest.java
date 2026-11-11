package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.user.LoginMfaGate;
import com.licensis.notaire.adapter.in.web.user.LoginMfaGate.Challenge;
import com.licensis.notaire.adapter.in.web.user.UserController;
import com.licensis.notaire.adapter.in.web.user.UserLoginExceptionHandler;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase.Result;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.GlobalExceptionHandler;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.domain.user.User;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.LoginAttemptService;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU78"})
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController MFA login (CU78, ADR-032)")
class UserLoginMfaControllerTest {

    private static final String LOGIN = "/api/v1/users/login";
    private static final String LOGIN_MFA = "/api/v1/users/login/mfa";
    private static final String BODY = "{\"name\":\"juan\",\"password\":\"secret\"}";
    private static final User JUAN = User.restore(1, "juan", "hash", true, "ADMIN", null, null, null, null, null, 0);

    @Mock private QueryUsersUseCase query;
    @Mock private ManageUsersUseCase manage;
    @Mock private AuthenticateUserUseCase authenticate;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private MetricsUtil metricsUtil;
    @Mock private LoginAttemptService loginAttemptService;
    @Mock private AuthCookieService authCookieService;
    @Mock private TokenRevocationService tokenRevocationService;
    @Mock private LoginMfaGate gate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(query, manage, authenticate, jwtTokenService,
                        metricsUtil, loginAttemptService, authCookieService, tokenRevocationService, gate))
                .setControllerAdvice(new UserLoginExceptionHandler(), new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("password step returns the challenge, no cookie, and does not clear the failure counter")
    void shouldReturnChallengeInsteadOfSession() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret")).thenReturn(new Result.Success(JUAN));
        when(gate.challenge(JUAN)).thenReturn(Optional.of(new Challenge("tok", false)));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.mfaRequired").value(true))
                .andExpect(jsonPath("$.mfaEnrollmentRequired").value(false))
                .andExpect(jsonPath("$.mfaToken").value("tok"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(loginAttemptService, never()).onLoginSucceeded(anyString());
        verifyNoInteractions(jwtTokenService);
        verify(metricsUtil).incrementCounter("login", "mfa_required");
    }

    @Test
    @DisplayName("password step flags enrolment-required separately")
    void shouldReturnEnrollmentRequired() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret")).thenReturn(new Result.Success(JUAN));
        when(gate.challenge(JUAN)).thenReturn(Optional.of(new Challenge("enrol", true)));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mfaRequired").value(false))
                .andExpect(jsonPath("$.mfaEnrollmentRequired").value(true))
                .andExpect(jsonPath("$.mfaToken").value("enrol"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));
        verify(metricsUtil).incrementCounter("login", "mfa_enrollment_required");
    }

    @Test
    @DisplayName("login/mfa with a valid code issues the session and clears the counter")
    void shouldIssueSessionAfterValidSecondFactor() throws Exception {
        when(gate.challengeSubject("tok")).thenReturn(Optional.of("juan"));
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(query.findByName("juan")).thenReturn(Optional.of(JUAN));
        when(gate.verifySecondFactor(JUAN, "123456", null)).thenReturn(true);
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt");
        when(authCookieService.createSessionCookie("jwt"))
                .thenReturn(ResponseCookie.from("notaire-auth-token", "jwt").httpOnly(true).build());

        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"tok\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));

        verify(loginAttemptService).onLoginSucceeded("juan");
    }

    @Test
    @DisplayName("login/mfa with a recovery code passes it to the second-factor check")
    void shouldAcceptRecoveryCode() throws Exception {
        when(gate.challengeSubject("tok")).thenReturn(Optional.of("juan"));
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(query.findByName("juan")).thenReturn(Optional.of(JUAN));
        when(gate.verifySecondFactor(JUAN, null, "ABCDE-FGHJK")).thenReturn(true);
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt");
        when(authCookieService.createSessionCookie("jwt"))
                .thenReturn(ResponseCookie.from("notaire-auth-token", "jwt").build());

        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"tok\",\"recoveryCode\":\"ABCDE-FGHJK\"}"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("login/mfa with a wrong code is rejected, counted and never logs the code")
    void shouldCountWrongCode() throws Exception {
        when(gate.challengeSubject("tok")).thenReturn(Optional.of("juan"));
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(query.findByName("juan")).thenReturn(Optional.of(JUAN));
        when(gate.verifySecondFactor(JUAN, "999999", null)).thenReturn(false);

        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"tok\",\"code\":\"999999\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(loginAttemptService).onLoginFailed("juan");
        verify(loginAttemptService, never()).onLoginSucceeded(anyString());
        verify(metricsUtil).incrementCounter("login", "mfa_failed");
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    @DisplayName("login/mfa with an invalid or expired challenge token is rejected without a lockout lookup")
    void shouldRejectInvalidChallenge() throws Exception {
        when(gate.challengeSubject("bad")).thenReturn(Optional.empty());

        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"bad\",\"code\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));

        verifyNoInteractions(loginAttemptService);
        verify(metricsUtil).incrementCounter("login", "mfa_invalid_challenge");
    }

    @Test
    @DisplayName("login/mfa answers 429 for a locked account without evaluating the code")
    void shouldRefuseLockedAccount() throws Exception {
        when(gate.challengeSubject("tok")).thenReturn(Optional.of("juan"));
        when(loginAttemptService.isLocked("juan")).thenReturn(true);

        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mfaToken\":\"tok\",\"code\":\"123456\"}"))
                .andExpect(status().isTooManyRequests());

        verify(gate, never()).verifySecondFactor(JUAN, "123456", null);
    }

    @Test
    @DisplayName("login/mfa rejects a body that carries no token")
    void shouldRequireToken() throws Exception {
        mockMvc.perform(post(LOGIN_MFA).contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"123456\"}"))
                .andExpect(status().isBadRequest());
    }
}
