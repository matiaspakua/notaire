package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("DocumentoPresentado controller — create/update/delete via DTO")
class DocumentoPresentadoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with tipoId, fecha and entregado")
    void shouldCreateDocumentoPresentadoWithDtoFields() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-06-01", "entregado": false}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoPresentado").isNumber())
                .andExpect(jsonPath("$.entregado").value(false));
    }

    @Test
    @DisplayName("Should return 201 when creating documento presentado with entregado true")
    void shouldCreateDocumentoPresentadoEntregado() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-07-15", "entregado": true}
                """;

        mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entregado").value(true));
    }

    @Test
    @DisplayName("Should return 200 when listing documentos presentados")
    void shouldListDocumentosPresentados() throws Exception {
        mockMvc.perform(get("/api/v1/documento-presentado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should return 200 and update documento presentado")
    void shouldUpdateDocumentoPresentado() throws Exception {
        String createBody = """
                {"tipoId": null, "fecha": "2024-01-01", "entregado": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Integer id = mapper.readTree(response).get("idDocumentoPresentado").asInt();

        String updateBody = """
                {"tipoId": null, "fecha": "2024-12-31", "entregado": true}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent documento presentado")
    void shouldReturn404WhenUpdatingMissingDocumento() throws Exception {
        String body = """
                {"tipoId": null, "fecha": "2024-01-01", "entregado": false}
                """;

        mockMvc.perform(put("/api/v1/documento-presentado/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 200 and delete an existing documento presentado")
    void shouldDeleteDocumentoPresentado() throws Exception {
        String createBody = """
                {"tipoId": null, "fecha": "2024-05-10", "entregado": false}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/documento-presentado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idDocumentoPresentado").asInt();

        mockMvc.perform(delete("/api/v1/documento-presentado/" + id))
                .andExpect(status().isOk());
    }
}
