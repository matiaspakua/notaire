package com.licensis.notaire.integration;

import com.licensis.notaire.config.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("JWT authentication integration tests")
class JwtAuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @DisplayName("Login with valid credentials returns JWT token")
    void shouldLoginAndReturnJwtToken() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login with wrong password returns valid=false and no token")
    void shouldRejectLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "wrongpassword"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("Login with unknown user returns valid=false")
    void shouldRejectLoginWithUnknownUser() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "nosuchuser", "password": "any"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("API request without a Bearer token is rejected with 401 (issue #552)")
    void shouldRejectApiRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API request with an invalid Bearer token is rejected with 401 (issue #552)")
    void shouldRejectApiRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API request with an expired Bearer token is rejected with 401 (issue #687)")
    void shouldRejectApiRequestWithExpiredToken() throws Exception {
        Object originalExpirationMs = ReflectionTestUtils.getField(jwtTokenService, "expirationMs");
        String expiredToken;
        try {
            ReflectionTestUtils.setField(jwtTokenService, "expirationMs", 1L);
            expiredToken = jwtTokenService.generateToken("admin");
            Thread.sleep(10);
        } finally {
            ReflectionTestUtils.setField(jwtTokenService, "expirationMs", originalExpirationMs);
        }

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid Bearer token is accepted in API requests")
    void shouldAcceptValidBearerTokenInApiRequest() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-LOGIN-12: Login with empty username and password returns 400 (#655)")
    void shouldRejectLoginWithEmptyCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "password": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("TC-LOGIN-13: Login with case-insensitive username succeeds")
    void shouldLoginWithCaseInsensitiveUsername() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "ADMIN", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC-LOGIN-14: Inactive user cannot login even with correct password")
    void shouldRejectLoginForInactiveUser() throws Exception {
        // First login as admin to get a token for creating a user
        String loginResponse = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse).get("token").asText();

        // Create an inactive user using the admin token (Bearer clients are
        // exempt from CSRF — issue #1620)
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("""
                        {"name": "inactive_user", "password": "password123", "type": "EMPLEADO", "active": false}
                        """))
                .andExpect(status().isCreated());

        // Attempt login with the inactive user — should fail even with correct password
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "inactive_user", "password": "password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    @DisplayName("Login sets HttpOnly SameSite auth cookie (issue #1051)")
    void shouldSetHttpOnlyAuthCookieOnLogin() throws Exception {
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                // Since #1618 the response also carries the script-readable
                // XSRF-TOKEN cookie, so the session cookie is no longer the
                // only — nor necessarily the first — Set-Cookie header
                // (issue #1620).
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("notaire-auth-token="),
                        containsString("HttpOnly"),
                        containsString("SameSite=Lax"),
                        containsString("Path=/")))));
    }

    @Test
    @DisplayName("API accepts auth cookie without Bearer (issue #1051)")
    void shouldAcceptAuthCookieWithoutBearer() throws Exception {
        var loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String cookieValue = authCookieValue(loginResult, "notaire-auth-token");

        mockMvc.perform(get("/api/v1/users")
                        .cookie(new jakarta.servlet.http.Cookie("notaire-auth-token", cookieValue)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Logout clears auth cookie (issue #1051)")
    void shouldClearAuthCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/v1/users/logout"))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(HttpHeaders.SET_COOKIE, hasItem(allOf(
                        containsString("notaire-auth-token="),
                        containsString("Max-Age=0")))));
    }

    /**
     * Returns the value of {@code name} from the response's {@code Set-Cookie}
     * headers. A response can carry several of them — the session cookie plus
     * {@code XSRF-TOKEN} since #1618 — so reading only the first one is wrong
     * (issue #1620).
     */
    private static String authCookieValue(MvcResult result, String name) {
        return result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(cookie -> cookie.startsWith(name + "="))
                .findFirst()
                .map(cookie -> extractCookieValue(cookie, name))
                .orElseThrow(() -> new IllegalStateException(
                        "Cookie " + name + " missing from Set-Cookie: "
                                + result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)));
    }

    private static String extractCookieValue(String setCookieHeader, String name) {
        String prefix = name + "=";
        int start = setCookieHeader.indexOf(prefix);
        if (start < 0) {
            throw new IllegalStateException("Cookie " + name + " missing from Set-Cookie: " + setCookieHeader);
        }
        int valueStart = start + prefix.length();
        int end = setCookieHeader.indexOf(';', valueStart);
        return end < 0 ? setCookieHeader.substring(valueStart) : setCookieHeader.substring(valueStart, end);
    }
}
