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
class RoleRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /roles with blank name returns 400")
    void shouldRejectCreateWithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "description": "desc", "active": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /roles with missing name returns 400")
    void shouldRejectCreateWithMissingName() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "desc", "active": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /roles with valid payload still succeeds")
    void shouldAcceptCreateWithValidPayload() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "roleValido655", "description": "desc", "active": true, "modulos": []}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /roles/{id} with blank name returns 400")
    void shouldRejectUpdateWithBlankName() throws Exception {
        mockMvc.perform(put("/api/v1/roles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "description": "desc", "active": true, "modulos": []}
                                """))
                .andExpect(status().isBadRequest());
    }
}
