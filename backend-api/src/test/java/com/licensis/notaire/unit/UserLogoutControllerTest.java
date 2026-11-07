package com.licensis.notaire.unit;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.licensis.notaire.adapter.in.web.user.UserController;
import com.licensis.notaire.application.port.in.user.AuthenticateUserUseCase;
import com.licensis.notaire.application.port.in.user.ManageUsersUseCase;
import com.licensis.notaire.application.port.in.user.QueryUsersUseCase;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.config.TokenRevocationService;
import com.licensis.notaire.observability.MetricsUtil;
import com.licensis.notaire.security.LoginAttemptService;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU84"})
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController logout")
class UserLogoutControllerTest {

    private static final String LOGOUT = "/api/v1/users/logout";

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
        when(authCookieService.cookieName()).thenReturn("notaire-auth-token");
        when(authCookieService.clearSessionCookie()).thenReturn(ResponseCookie.from("notaire-auth-token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build());
        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(
                        query,
                        manage,
                        authenticate,
                        jwtTokenService,
                        metricsUtil,
                        loginAttemptService,
                        authCookieService,
                        tokenRevocationService))
                .build();
    }

    @Test
    @DisplayName("POST /logout revokes the bearer token and returns ok")
    void shouldRevokePresentedToken() throws Exception {
        mockMvc.perform(post(LOGOUT).header(HttpHeaders.AUTHORIZATION, "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"ok\":true}"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(tokenRevocationService).revoke("jwt-token");
    }

    @Test
    @DisplayName("POST /logout still returns ok when no token is presented")
    void shouldSucceedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(post(LOGOUT))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"ok\":true}"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(tokenRevocationService).revoke(isNull());
    }
}
