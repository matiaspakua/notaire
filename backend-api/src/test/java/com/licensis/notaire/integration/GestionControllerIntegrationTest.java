package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@DisplayName("Gestion controller — create validates data before hitting the database")
class GestionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona() throws Exception {
        String body = """
                {"nombre": "Escribano IT", "apellido": "Gestion IT", "numeroIdentificacion": "42000001",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    @Test
    @DisplayName("Should return 400 with a body, not a bare 500, when encabezado is missing")
    void shouldReturn400WhenEncabezadoIsMissing() throws Exception {
        Integer personaId = createPersona();
        String body = """
                {"fechaInicio": "2026-01-01", "numero": 9101,
                 "fkIdPersonaEscribano": {"idPersona": %d}}
                """.formatted(personaId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Should return 201 when creating a gestion with all required fields")
    void shouldCreateGestionWithValidData() throws Exception {
        Integer personaId = createPersona();
        String body = """
                {"encabezado": "Gestion IT", "fechaInicio": "2026-01-01", "numero": 9102,
                 "fkIdPersonaEscribano": {"idPersona": %d}}
                """.formatted(personaId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idGestion").isNumber());
    }
}
