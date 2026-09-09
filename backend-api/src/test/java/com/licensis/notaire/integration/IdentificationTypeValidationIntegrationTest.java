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
class IdentificationTypeValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /type-identificacion with blank name returns 400")
    void shouldRejectCreateWithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "characters": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /type-identificacion with missing name returns 400")
    void shouldRejectCreateWithMissingName() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"characters": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /type-identificacion with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/tipo-identificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "typeValido655", "characters": "9"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /type-identificacion/{id} with blank name returns 400")
    void shouldRejectUpdateWithBlankName() throws Exception {
        mockMvc.perform(put("/api/v1/tipo-identificacion/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "characters": "9"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
