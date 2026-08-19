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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
                                {"nombre": "admin", "contrasenia": "admin"}
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
                                {"nombre": "admin", "contrasenia": "wrongpassword"}
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
                                {"nombre": "nosuchuser", "contrasenia": "any"}
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
                                {"nombre": "admin", "contrasenia": "admin"}
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
                                {"nombre": "", "contrasenia": ""}
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
                                {"nombre": "ADMIN", "contrasenia": "admin"}
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
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String token = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(loginResponse).get("token").asText();

        // Create an inactive user using the admin token
        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("""
                        {"nombre": "inactive_user", "contrasenia": "password123", "tipo": "EMPLEADO", "activo": false}
                        """))
                .andExpect(status().isCreated());

        // Attempt login with the inactive user — should fail even with correct password
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "inactive_user", "contrasenia": "password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));
    }
}
