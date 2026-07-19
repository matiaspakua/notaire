package com.licensis.notaire.integration;

import com.licensis.notaire.config.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
class ApiSecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JwtTokenService jwtTokenService;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void shouldRejectApiRequestWithoutAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectApiRequestWithInvalidAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/1")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowApiRequestWithValidAuthenticationToken() throws Exception {
        String token = jwtTokenService.generateToken("admin");

        int responseStatus = mockMvc.perform(get("/api/v1/usuarios/1")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();

        assertThat(responseStatus).isNotEqualTo(401);
    }

    @Test
    void shouldAllowLoginWithoutAuthenticationToken() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "admin",
                                  "contrasenia": "admin"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
