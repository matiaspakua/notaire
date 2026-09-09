package com.licensis.notaire.integration;

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
class UserControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should return activo field (not status) in GET response")
    void shouldReturnActiveFieldInGetResponse() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").exists());
    }

    @Test
    @DisplayName("Should create usuario with activo=true and return activo in response")
    void shouldCreateUserWithActive() throws Exception {
        String body = """
                {"name": "testuser_create", "password": "pass", "type": "EMPLEADO", "active": true}
                """;

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").isNumber())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("Should update usuario and preserve activo=true, not silently deactivate")
    void shouldUpdateUserWithoutDeactivating() throws Exception {
        String createBody = """
                {"name": "testuser_upd", "password": "pass", "type": "EMPLEADO", "active": true}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUser").asInt();

        String updateBody = """
                {"name": "testuser_upd_renamed", "password": "", "type": "ADMIN", "active": true}
                """;

        mockMvc.perform(put("/api/v1/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.name").value("testuser_upd_renamed"))
                .andExpect(jsonPath("$.type").value("ADMIN"));
    }

    @Test
    @DisplayName("Should set activo=false when update sends activo=false")
    void shouldDeactivateUserWhenActiveFalse() throws Exception {
        String createBody = """
                {"name": "testuser_deact", "password": "pass", "type": "EMPLEADO", "active": true}
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUser").asInt();

        String updateBody = """
                {"name": "testuser_deact", "password": "", "type": "EMPLEADO", "active": false}
                """;

        mockMvc.perform(put("/api/v1/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent usuario")
    void shouldReturn404WhenUpdatingMissingUser() throws Exception {
        String body = """
                {"name": "ghost", "password": "x", "type": "EMPLEADO", "active": true}
                """;

        mockMvc.perform(put("/api/v1/usuarios/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }
}
