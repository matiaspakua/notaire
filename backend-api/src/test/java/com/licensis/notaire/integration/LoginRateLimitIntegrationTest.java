package com.licensis.notaire.integration;

import com.licensis.notaire.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@TestPropertySource(properties = {
        "security.login.max-attempts=3",
        "security.login.lockout-duration-ms=60000",
        "spring.datasource.url=jdbc:h2:mem:notaire_test_rate_limit;MODE=PostgreSQL;"
                + "DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
})
@DisplayName("Login rate limiting / account lockout (issue #560)")
class LoginRateLimitIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LoginAttemptService loginAttemptService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        loginAttemptService.onLoginSucceeded("admin");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private void attemptLoginWithWrongPassword() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre": "admin", "contrasenia": "wrongpassword"}
                        """));
    }

    @Test
    @DisplayName("Locks the account out after reaching the max failed attempts")
    void shouldLockAccountAfterMaxFailedAttempts() throws Exception {
        attemptLoginWithWrongPassword();
        attemptLoginWithWrongPassword();
        attemptLoginWithWrongPassword();

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    @DisplayName("Does not lock the account before reaching the max failed attempts")
    void shouldNotLockAccountBeforeMaxFailedAttempts() throws Exception {
        attemptLoginWithWrongPassword();

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    @DisplayName("A successful login resets the failed attempt counter")
    void shouldResetCounterAfterSuccessfulLogin() throws Exception {
        attemptLoginWithWrongPassword();
        attemptLoginWithWrongPassword();

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        attemptLoginWithWrongPassword();
        attemptLoginWithWrongPassword();

        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "admin", "contrasenia": "admin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }
}
