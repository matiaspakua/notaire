package com.licensis.notaire.unit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@RequirementCoverage({"CU84"})
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController login")
class UserLoginControllerTest {

    private static final String LOGIN = "/api/v1/users/login";
    private static final String BODY = "{\"name\":\"juan\",\"password\":\"secret\"}";

    @Mock
    private QueryUsersUseCase query;

    @Mock
    private ManageUsersUseCase manage;

    @Mock
    private AuthenticateUserUseCase authenticate;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private MetricsUtil metricsUtil;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private AuthCookieService authCookieService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                        query,
                        manage,
                        authenticate,
                        jwtTokenService,
                        metricsUtil,
                        loginAttemptService,
                        authCookieService,
                        tokenRevocationService,
                        org.mockito.Mockito.mock(com.licensis.notaire.adapter.in.web.user.LoginMfaGate.class)))
                .setControllerAdvice(new UserLoginExceptionHandler(), new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /login returns the typed success body and session cookie")
    void shouldReturnSuccessBodyAndSessionCookie() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret")).thenReturn(new Result.Success(user(null, null, null)));
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt-token");
        when(authCookieService.createSessionCookie("jwt-token")).thenReturn(sessionCookie());

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.idUser").value(1))
                .andExpect(jsonPath("$.name").value("juan"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.type").value("Escribano"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.person").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString(
                        "notaire-auth-token")));

        verify(loginAttemptService).onLoginSucceeded("juan");
        verify(metricsUtil).incrementCounter("login", "success");
    }

    @Test
    @DisplayName("POST /login includes person only when the user has a person id")
    void shouldIncludePersonWhenPresent() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret"))
                .thenReturn(new Result.Success(user(9, "Ada", "Lovelace")));
        when(jwtTokenService.generateToken("juan")).thenReturn("jwt-token");
        when(authCookieService.createSessionCookie("jwt-token")).thenReturn(sessionCookie());

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.person.idPerson").value(9))
                .andExpect(jsonPath("$.person.firstName").value("Ada"))
                .andExpect(jsonPath("$.person.lastName").value("Lovelace"));
    }

    @Test
    @DisplayName("POST /login returns 200 valid=false when the user is missing")
    void shouldRejectUnknownUser() throws Exception {
        reject(AuthenticateUserUseCase.Failure.NOT_FOUND, "not_found");
    }

    @Test
    @DisplayName("POST /login returns 200 valid=false for a wrong password")
    void shouldRejectBadCredentials() throws Exception {
        reject(AuthenticateUserUseCase.Failure.BAD_CREDENTIALS, "bad_credentials");
    }

    @Test
    @DisplayName("POST /login returns 200 valid=false for an inactive user")
    void shouldRejectInactiveUser() throws Exception {
        reject(AuthenticateUserUseCase.Failure.INACTIVE, "inactive");
    }

    @Test
    @DisplayName("POST /login returns 200 valid=false when authentication throws")
    void shouldHideUnexpectedErrorsAsInvalidLogin() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret")).thenThrow(new IllegalStateException("db down"));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"valid\":false}"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(metricsUtil).incrementCounter("login", "error");
        verify(loginAttemptService, never()).onLoginFailed("juan");
        verify(loginAttemptService, never()).onLoginSucceeded("juan");
    }

    @Test
    @DisplayName("POST /login returns 429 when the account is locked")
    void shouldRejectLockedAccount() throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(true);

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value(
                        "Account temporarily locked after too many failed attempts."))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(metricsUtil).incrementCounter("login", "locked");
        verify(authenticate, never()).authenticate("juan", "secret");
        verify(loginAttemptService, never()).onLoginFailed("juan");
    }

    private void reject(AuthenticateUserUseCase.Failure failure, String metric) throws Exception {
        when(loginAttemptService.isLocked("juan")).thenReturn(false);
        when(authenticate.authenticate("juan", "secret")).thenReturn(new Result.Failed(failure));

        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"valid\":false}"))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(metricsUtil).incrementCounter("login", metric);
        verify(loginAttemptService).onLoginFailed("juan");
        verify(loginAttemptService, never()).onLoginSucceeded("juan");
    }

    private static User user(Integer personId, String firstName, String lastName) {
        return User.restore(1, "juan", "hash", true, "Escribano", personId, firstName, lastName, null, null, 0);
    }

    private static ResponseCookie sessionCookie() {
        return ResponseCookie.from("notaire-auth-token", "jwt-token").httpOnly(true).path("/").build();
    }
}
