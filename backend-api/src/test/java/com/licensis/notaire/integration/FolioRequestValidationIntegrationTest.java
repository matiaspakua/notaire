package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Folio request validation (issue #655)")
class FolioRequestValidationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("POST /folio with blank estado returns 400")
    void shouldRejectCreateWithBlankEstado() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero": 1655, "anio": 2026, "estado": "", "tipoFolioId": 1, "escribanoId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /folio with missing estado returns 400")
    void shouldRejectCreateWithMissingEstado() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero": 1656, "anio": 2026, "tipoFolioId": 1, "escribanoId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /folio/{id} with blank estado returns 400")
    void shouldRejectUpdateWithBlankEstado() throws Exception {
        String response = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero": 1657, "anio": 2026, "estado": "Nuevo", "tipoFolioId": 1, "escribanoId": 1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = new ObjectMapper().readTree(response).get("idFolio").asInt();

        mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero": 1657, "anio": 2026, "estado": "", "tipoFolioId": 1, "escribanoId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
