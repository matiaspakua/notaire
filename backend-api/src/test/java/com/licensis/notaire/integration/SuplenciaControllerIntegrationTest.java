package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@DisplayName("Suplencia controller — CU23 get by id serializes detached lazy relations")
class SuplenciaControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPersona(String nombre, String numeroIdentificacion) throws Exception {
        String body = """
                {"nombre": "%s", "apellido": "Suplencia IT", "numeroIdentificacion": "%s",
                 "esCliente": false, "tipoIdentificacion": {"idTipoIdentificacion": 1}}
                """.formatted(nombre, numeroIdentificacion);
        MvcResult result = mockMvc.perform(post("/api/v1/personas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idPersona").asInt();
    }

    private Integer createSuplencia() throws Exception {
        Integer suplente = createPersona("Suplente IT", "41000001");
        Integer suplantado = createPersona("Suplantado IT", "41000002");
        String body = """
                {"fechaInicio": "2026-01-01", "fechaFin": "2026-01-31",
                 "fkIdSuplente": {"idPersona": %d}, "fkIdSuplantado": {"idPersona": %d}}
                """.formatted(suplente, suplantado);
        MvcResult result = mockMvc.perform(post("/api/v1/suplencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idSuplencia").asInt();
    }

    @Test
    @DisplayName("Should return 200 with suplencia and personas when getting existing suplencia by id")
    void shouldReturnSuplenciaByIdWithPersonas() throws Exception {
        Integer id = createSuplencia();

        mockMvc.perform(get("/api/v1/suplencia/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idSuplencia").value(id))
                .andExpect(jsonPath("$.fkIdSuplente.idPersona").isNumber())
                .andExpect(jsonPath("$.fkIdSuplantado.idPersona").isNumber());
    }

    @Test
    @DisplayName("Should return 404 when getting nonexistent suplencia by id")
    void shouldReturn404ForNonexistentSuplencia() throws Exception {
        mockMvc.perform(get("/api/v1/suplencia/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should actually remove suplencia row so a subsequent get returns 404")
    void shouldDeleteSuplenciaAndMakeItUnreachable() throws Exception {
        Integer id = createSuplencia();

        mockMvc.perform(delete("/api/v1/suplencia/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/suplencia/" + id))
                .andExpect(status().isNotFound());
    }
}
