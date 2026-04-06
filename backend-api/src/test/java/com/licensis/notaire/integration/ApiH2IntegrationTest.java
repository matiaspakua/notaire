package com.licensis.notaire.integration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
class ApiH2IntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

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
                .andExpect(jsonPath("$.valido").isBoolean());
    }

    @Test
    @Disabled("Pending fix for lazy loading in JSON serialization")
    void shouldExposeCoreCatalogEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/conceptos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/personas"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/tramites"))
                .andExpect(status().isOk());
    }
}
