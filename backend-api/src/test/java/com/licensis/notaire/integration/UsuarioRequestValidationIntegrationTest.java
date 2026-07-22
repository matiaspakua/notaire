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
class UsuarioRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /usuarios with blank nombre returns 400")
    void shouldRejectCreateWithBlankNombre() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "contrasenia": "pass", "tipo": "EMPLEADO", "activo": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios with missing nombre returns 400")
    void shouldRejectCreateWithMissingNombre() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contrasenia": "pass", "tipo": "EMPLEADO", "activo": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios with blank tipo returns 400")
    void shouldRejectCreateWithBlankTipo() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "someuser", "contrasenia": "pass", "tipo": "", "activo": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /usuarios with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "valid_user_561", "contrasenia": "pass", "tipo": "EMPLEADO", \
                                "activo": true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /usuarios/{id} with blank nombre returns 400")
    void shouldRejectUpdateWithBlankNombre() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "contrasenia": "", "tipo": "EMPLEADO", "activo": true}
                                """))
                .andExpect(status().isBadRequest());
    }
}
