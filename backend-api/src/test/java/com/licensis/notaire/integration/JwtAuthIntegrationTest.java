package com.licensis.notaire.integration;

import com.licensis.notaire.config.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
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
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Login with wrong password returns valido=false and no token")
    void shouldRejectLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "wrongpassword"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("Login with unknown user returns valido=false")
    void shouldRejectLoginWithUnknownUser() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "nosuchuser", "password": "any"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("API request without a Bearer token is rejected with 401 (issue #552)")
    void shouldRejectApiRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API request with an invalid Bearer token is rejected with 401 (issue #552)")
    void shouldRejectApiRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
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

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid Bearer token is accepted in API requests")
    void shouldAcceptValidBearerTokenInApiRequest() throws Exception {
        String loginResponse = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse).get("token").asText();

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-LOGIN-12: Login with empty username and password returns valido=false")
    void shouldRejectLoginWithEmptyCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "password": ""}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("TC-LOGIN-13: Login with case-insensitive username succeeds")
    void shouldLoginWithCaseInsensitiveUsername() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "ADMIN", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("TC-LOGIN-14: Inactive user cannot login even with correct password")
    void shouldRejectLoginForInactiveUser() throws Exception {
        // First login as admin to get a token for creating a user
        String loginResponse = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse).get("token").asText();

        // Create an inactive user using the admin token
        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("""
                        {"name": "inactive_user", "password": "password123", "type": "EMPLEADO", "active": false}
                        """))
                .andExpect(status().isCreated());

        // Attempt login with the inactive user — should fail even with correct password
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "inactive_user", "password": "password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("Login sets HttpOnly SameSite auth cookie (issue #1051)")
    void shouldSetHttpOnlyAuthCookieOnLogin() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(header().string("Set-Cookie", containsString("notaire-auth-token=")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
                .andExpect(header().string("Set-Cookie", containsString("Path=/")));
    }

    @Test
    @DisplayName("API accepts auth cookie without Bearer (issue #1051)")
    void shouldAcceptAuthCookieWithoutBearer() throws Exception {
        var loginResult = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String setCookie = loginResult.getResponse().getHeader("Set-Cookie");
        String cookieValue = extractCookieValue(setCookie, "notaire-auth-token");

        mockMvc.perform(get("/api/v1/usuarios")
                        .cookie(new jakarta.servlet.http.Cookie("notaire-auth-token", cookieValue)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Logout clears auth cookie (issue #1051)")
    void shouldClearAuthCookieOnLogout() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("notaire-auth-token=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
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
