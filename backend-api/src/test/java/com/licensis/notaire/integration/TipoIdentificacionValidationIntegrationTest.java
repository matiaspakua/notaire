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
@DisplayName("TipoIdentificacion request validation (issue #655)")
class TipoIdentificacionValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /tipo-identificacion with blank nombre returns 400")
    void shouldRejectCreateWithBlankNombre() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "caracteres": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tipo-identificacion with missing nombre returns 400")
    void shouldRejectCreateWithMissingNombre() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"caracteres": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /tipo-identificacion with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "tipo_valido_655", "caracteres": "9"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /tipo-identificacion/{id} with blank nombre returns 400")
    void shouldRejectUpdateWithBlankNombre() throws Exception {
        mockMvc.perform(put("/api/v1/tipo-identificacion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "", "caracteres": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
