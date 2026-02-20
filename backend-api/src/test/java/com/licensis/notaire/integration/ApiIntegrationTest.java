package com.licensis.notaire.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAuthenticateDefaultAdminUser() throws Exception {
        mockMvc.perform(post("/api/v1/usuarios/login")
                        .contentType("application/json")
                        .content("""
                                {
                                  "nombre": "admin",
                                  "contrasenia": "admin"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    void shouldExposeCoreCatalogEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/presupuestos"))
                .andExpect(status().isOk());
    }
}
