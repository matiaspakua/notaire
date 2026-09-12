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
    @DisplayName("POST /folio with blank status returns 400")
    void shouldRejectCreateWithBlankStatus() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": 1655, "year": 2026, "status": "", "typeFolioId": 1, "notaryId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /folio with missing status returns 400")
    void shouldRejectCreateWithMissingStatus() throws Exception {
        mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": 1656, "year": 2026, "typeFolioId": 1, "notaryId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /folio/{id} with blank status returns 400")
    void shouldRejectUpdateWithBlankStatus() throws Exception {
        String response = mockMvc.perform(post("/api/v1/folio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": 1657, "year": 2026, "status": "Nuevo", "typeFolioId": 1, "notaryId": 1}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = new ObjectMapper().readTree(response).get("idFolio").asInt();

        mockMvc.perform(put("/api/v1/folio/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"number": 1657, "year": 2026, "status": "", "typeFolioId": 1, "notaryId": 1}
                                """))
                .andExpect(status().isBadRequest());
    }
}
