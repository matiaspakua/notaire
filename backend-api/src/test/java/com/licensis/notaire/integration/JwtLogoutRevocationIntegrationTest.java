package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Logout revokes the presented JWT server-side (issue #676): a copied token can
 * no longer be replayed after logout, while other sessions of the same user stay valid.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("JWT logout revocation integration tests (issue #676)")
class JwtLogoutRevocationIntegrationTest {

    private static final String COOKIE_NAME = "notaire-auth-token";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String loginAsAdmin() throws Exception {
        String body = mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "admin", "password": "admin"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonMapper.builder().build().readTree(body).get("token").asText();
    }

    @Test
    @DisplayName("Bearer token used after logout is rejected; another session stays valid")
    void bearerTokenIsRejectedAfterLogout() throws Exception {
        String loggedOut = loginAsAdmin();
        String otherSession = loginAsAdmin();

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + loggedOut))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/usuarios/logout").header("Authorization", "Bearer " + loggedOut))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + loggedOut))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + otherSession))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Cookie token replayed after logout is rejected")
    void cookieTokenIsRejectedAfterLogout() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/v1/usuarios/logout").cookie(new Cookie(COOKIE_NAME, token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios").cookie(new Cookie(COOKIE_NAME, token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout without a token or with an invalid one still succeeds")
    void logoutWithoutValidTokenStillSucceeds() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
        mockMvc.perform(post("/api/v1/usuarios/logout").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    @DisplayName("Logging out twice with the same token is harmless")
    void repeatedLogoutIsIdempotent() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(post("/api/v1/usuarios/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/usuarios/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
