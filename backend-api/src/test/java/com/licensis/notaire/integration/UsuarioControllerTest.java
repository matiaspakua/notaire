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
@DisplayName("Usuario controller — activo field mapping")
class UsuarioControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return activo field (not estado) in GET response")
    void shouldReturnActivoFieldInGetResponse() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activo").exists());
    }

    @Test
    @DisplayName("Should create usuario with activo=true and return activo in response")
    void shouldCreateUsuarioWithActivo() throws Exception {
        String body = """
                {"nombre": "testuser_create", "contrasenia": "pass", "tipo": "EMPLEADO", "activo": true}
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").isNumber())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("Should update usuario and preserve activo=true, not silently deactivate")
    void shouldUpdateUsuarioWithoutDeactivating() throws Exception {
        String createBody = """
                {"nombre": "testuser_upd", "contrasenia": "pass", "tipo": "EMPLEADO", "activo": true}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        String updateBody = """
                {"nombre": "testuser_upd_renamed", "contrasenia": "", "tipo": "ADMIN", "activo": true}
                """;

        mockMvc.perform(put("/api/v1/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.nombre").value("testuser_upd_renamed"))
                .andExpect(jsonPath("$.tipo").value("ADMIN"));
    }

    @Test
    @DisplayName("Should set activo=false when update sends activo=false")
    void shouldDeactivateUsuarioWhenActivoFalse() throws Exception {
        String createBody = """
                {"nombre": "testuser_deact", "contrasenia": "pass", "tipo": "EMPLEADO", "activo": true}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        String updateBody = """
                {"nombre": "testuser_deact", "contrasenia": "", "tipo": "EMPLEADO", "activo": false}
                """;

        mockMvc.perform(put("/api/v1/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent usuario")
    void shouldReturn404WhenUpdatingMissingUsuario() throws Exception {
        String body = """
                {"nombre": "ghost", "contrasenia": "x", "tipo": "EMPLEADO", "activo": true}
                """;

        mockMvc.perform(put("/api/v1/usuarios/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
