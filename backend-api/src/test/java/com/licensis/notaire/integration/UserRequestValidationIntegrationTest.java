package com.licensis.notaire.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Usuario request validation (issue #561)")
class UserRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /users with blank name returns 400")
    void shouldRejectCreateWithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "password": "pass", "type": "EMPLEADO", "active": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users with missing name returns 400")
    void shouldRejectCreateWithMissingName() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password": "pass", "type": "EMPLEADO", "active": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users with blank type returns 400")
    void shouldRejectCreateWithBlankType() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "someuser", "password": "pass", "type": "", "active": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "valid_user_561", "password": "pass", "type": "EMPLEADO", \
                                "active": true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /users/{id} with blank name returns 400")
    void shouldRejectUpdateWithBlankName() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "password": "", "type": "EMPLEADO", "active": true}
                                """))
                .andExpect(status().isBadRequest());
    }
}
