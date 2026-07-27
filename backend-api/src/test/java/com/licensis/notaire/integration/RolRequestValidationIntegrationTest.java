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
@DisplayName("Rol request validation (issue #655)")
class RolRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /roles with blank nombre returns 400")
    void shouldRejectCreateWithBlankNombre() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "descripcion": "desc", "activo": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /roles with missing nombre returns 400")
    void shouldRejectCreateWithMissingNombre() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"descripcion": "desc", "activo": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /roles with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "rol_valido_655", "descripcion": "desc", "activo": true, "modulos": []}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /roles/{id} with blank nombre returns 400")
    void shouldRejectUpdateWithBlankNombre() throws Exception {
        mockMvc.perform(put("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "descripcion": "desc", "activo": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }
}
