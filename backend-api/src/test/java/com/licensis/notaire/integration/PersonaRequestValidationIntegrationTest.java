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
@DisplayName("Persona request validation (issue #655)")
class PersonaRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /personas with blank nombre returns 400")
    void shouldRejectCreateWithBlankNombre() throws Exception {
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "apellido": "Perez", "numeroIdentificacion": "12345678", "esCliente": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /personas with missing apellido returns 400")
    void shouldRejectCreateWithMissingApellido() throws Exception {
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana", "numeroIdentificacion": "12345678", "esCliente": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /personas with blank numeroIdentificacion returns 400")
    void shouldRejectCreateWithBlankNumeroIdentificacion() throws Exception {
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana", "apellido": "Perez", "numeroIdentificacion": "", "esCliente": true}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /personas with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Ana", "apellido": "Perez655", "numeroIdentificacion": "99655321", "esCliente": true}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /personas/{id} with blank nombre returns 400")
    void shouldRejectUpdateWithBlankNombre() throws Exception {
        mockMvc.perform(put("/api/v1/personas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "apellido": "Garcia", "numeroIdentificacion": "20123456", "esCliente": false}
                                """))
                .andExpect(status().isBadRequest());
    }
}
