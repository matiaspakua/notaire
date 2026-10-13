package com.licensis.notaire.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.licensis.notaire.business.User;
import com.licensis.notaire.config.AuthCookieService;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.UserRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that the API filter chain enforces CSRF protection according to
 * CU78 / issue #1618, and that the two legitimate clients keep working after
 * the #1619 regression (issue #1620):
 *
 * <ul>
 *   <li>the browser BFF sends the raw {@code XSRF-TOKEN} cookie back as the
 *       {@code X-XSRF-TOKEN} header (double-submit);</li>
 *   <li>Bearer-token clients (Bruno, mobile, server-to-server) are exempt
 *       because a token they must attach explicitly cannot be replayed by a
 *       third-party site.</li>
 * </ul>
 *
 * <p>These tests deliberately avoid
 * {@code SecurityMockMvcRequestPostProcessors.csrf()}: it reflectively swaps the
 * shared {@code CsrfFilter}'s token repository for a permissive test one, which
 * disables enforcement for the rest of the JVM and hides the very regression
 * these tests guard against. Real cookies and real tokens are used instead.</p>
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("CSRF protection — CU78 / issue #1618")
class CsrfProtectionIntegrationTest {

    private static final String PROCEDURE_TYPE_BODY =
            "{\"name\":\"csrf-probe-%s\",\"notes\":\"created by CSRF integration test\"}";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthCookieService authCookieService;

    private MockMvc mockMvc;
    private String adminToken;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        String name = "csrf-" + UUID.randomUUID();
        userRepository.save(new User(null, name, "not-a-login-password", true, "Escribano"));
        adminToken = jwtTokenService.generateToken(name);
    }

    private String uniqueProcedureTypeBody() {
        return PROCEDURE_TYPE_BODY.formatted(UUID.randomUUID());
    }

    @Test
    @DisplayName("login endpoint is exempt from CSRF — requests without a token are accepted")
    void loginEndpointShouldBeExemptFromCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("login delivers the XSRF-TOKEN cookie beside the session cookie (#1620)")
    void loginShouldIssueTheXsrfCookieBesideTheSessionCookie() throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"admin","password":"admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        List<String> setCookie = login.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).as("login must keep setting the session cookie")
                .anyMatch(header -> header.startsWith(authCookieService.cookieName() + "="));

        // The login response sets its own Set-Cookie header. If that is written
        // through the ResponseEntity header map it replaces every Set-Cookie
        // already on the response and silently drops the token cookie, leaving
        // the browser unable to send X-XSRF-TOKEN until some later GET response.
        List<String> xsrfCookies = setCookie.stream()
                .filter(header -> header.startsWith("XSRF-TOKEN="))
                .toList();
        assertThat(xsrfCookies).as("login must also deliver the double-submit token cookie").hasSize(1);
        assertThat(xsrfCookies.get(0)).as("the token cookie must be script-readable").doesNotContain("HttpOnly");
    }

    @Test
    @DisplayName("unsafe API request without credentials is rejected with 403 by the CSRF filter")
    void unsafeApiRequestWithoutCsrfTokenShouldBeRejected() throws Exception {
        mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Bearer-token clients are exempt — a state-changing request succeeds without an XSRF token (#1620)")
    void bearerTokenRequestShouldBeExemptFromCsrf() throws Exception {
        // Regression from #1619: Bruno API tests and the E2E seed helpers hit
        // every mutating endpoint with a Bearer token and no CSRF token.
        mockMvc.perform(post("/api/v1/procedure-types")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uniqueProcedureTypeBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("browser double-submit works — the raw XSRF-TOKEN cookie echoed as X-XSRF-TOKEN is accepted (#1620)")
    void browserCookieDoubleSubmitShouldBeAccepted() throws Exception {
        // 1. Any response through the API chain issues the (non-HttpOnly) token cookie.
        MvcResult firstResponse = mockMvc.perform(get("/api/v1/people")
                        .cookie(new Cookie(authCookieService.cookieName(), adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie xsrf = firstResponse.getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrf).as("the API chain must issue the XSRF-TOKEN cookie").isNotNull();

        // 2. The browser client (frontend/src/lib/api-client.ts) reads that cookie
        //    value and echoes it verbatim in the header. Spring Security must
        //    resolve header-supplied values without the XOR mask it applies to
        //    server-rendered tokens, otherwise this is always a 403.
        mockMvc.perform(post("/api/v1/procedure-types")
                        .cookie(new Cookie(authCookieService.cookieName(), adminToken), xsrf)
                        .header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uniqueProcedureTypeBody()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("cookie-authenticated request without the XSRF header is still rejected with 403")
    void cookieAuthenticatedRequestWithoutXsrfHeaderShouldBeRejected() throws Exception {
        MvcResult firstResponse = mockMvc.perform(get("/api/v1/people")
                        .cookie(new Cookie(authCookieService.cookieName(), adminToken)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie xsrf = firstResponse.getResponse().getCookie("XSRF-TOKEN");
        assertThat(xsrf).isNotNull();

        mockMvc.perform(post("/api/v1/procedure-types")
                        .cookie(new Cookie(authCookieService.cookieName(), adminToken), xsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(uniqueProcedureTypeBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("an authenticated request does not rotate the XSRF-TOKEN cookie (#1620)")
    void authenticatedRequestShouldNotRotateTheXsrfCookie() throws Exception {
        Cookie xsrf = new Cookie("XSRF-TOKEN", UUID.randomUUID().toString());

        MvcResult response = mockMvc.perform(get("/api/v1/people")
                        .cookie(new Cookie(authCookieService.cookieName(), adminToken), xsrf))
                .andExpect(status().isOk())
                .andReturn();

        // Spring Security's default CsrfAuthenticationStrategy deletes and
        // re-issues the CSRF cookie on every authenticated request. For a
        // stateless double-submit nonce that rotates the cookie out from under
        // a client that reads document.cookie and then sends the request: the
        // concurrent responses of a dashboard load leave the echoed header and
        // the browser cookie disagreeing, and the next write is a 403.
        assertThat(response.getResponse().getHeaders(HttpHeaders.SET_COOKIE))
                .as("the double-submit nonce must stay stable while the session is unchanged")
                .noneMatch(header -> header.startsWith("XSRF-TOKEN="));
    }
}
