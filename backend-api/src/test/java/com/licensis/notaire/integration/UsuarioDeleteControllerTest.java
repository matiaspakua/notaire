package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

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
import com.licensis.notaire.negocio.RegistroAuditoria;
import com.licensis.notaire.negocio.Usuario;
import com.licensis.notaire.repository.RegistroAuditoriaRepository;
import com.licensis.notaire.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Usuario delete — user is actually removed from DB")
class UsuarioDeleteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroAuditoriaRepository registroAuditoriaRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should delete usuario and return 404 on subsequent GET")
    void shouldDeleteUsuarioAndReturnNotFoundOnSubsequentGet() throws Exception {
        // Create a user to delete (using persona id=1 from V2 test data)
        String createBody = """
                {
                  "nombre": "to_be_deleted",
                  "contrasenia": "pass",
                  "tipo": "EMPLEADO",
                  "estado": true,
                  "fkIdPersona": {"idPersona": 1}
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        // Delete the user
        mockMvc.perform(delete("/api/v1/usuarios/" + id))
                .andExpect(status().isNoContent());

        // Verify the user is actually gone
        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent usuario")
    void shouldReturn404WhenDeletingNonExistentUsuario() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should remove deleted usuario from list response")
    void shouldNotIncludeDeletedUsuarioInList() throws Exception {
        String createBody = """
                {
                  "nombre": "to_be_removed_from_list",
                  "contrasenia": "pass",
                  "tipo": "EMPLEADO",
                  "estado": true,
                  "fkIdPersona": {"idPersona": 1}
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        // Verify user exists in the list
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(jsonPath("$[?(@.idUsuario == " + id + ")]").exists());

        // Delete
        mockMvc.perform(delete("/api/v1/usuarios/" + id))
                .andExpect(status().isNoContent());

        // Verify user is not in the list anymore
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(jsonPath("$[?(@.idUsuario == " + id + ")]").doesNotExist());
    }

    @Test
    @DisplayName("Should delete usuario and cascade-remove associated audit records")
    void shouldDeleteUsuarioWithAuditRecordsViaCascade() throws Exception {
        // Create user via API (sets fkIdPersona=1 from seeded test data)
        String createBody = """
                {
                  "nombre": "user_with_audit",
                  "contrasenia": "pass",
                  "tipo": "EMPLEADO",
                  "estado": true,
                  "fkIdPersona": {"idPersona": 1}
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer userId = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUsuario").asInt();

        // Simulate a prior operation — create audit record referencing this user
        Usuario savedUser = usuarioRepository.findById(userId).orElseThrow();
        RegistroAuditoria audit = new RegistroAuditoria();
        audit.setFecha(new Date());
        audit.setModulo("Usuarios");
        audit.setDetalleOperacion("POST");
        audit.setFkIdUsuario(savedUser);
        registroAuditoriaRepository.save(audit);

        // Verify audit record exists before delete
        List<RegistroAuditoria> before = registroAuditoriaRepository.findByFkIdUsuarioIdUsuario(userId);
        assertThat(before).isNotEmpty();

        // Delete user via API endpoint
        mockMvc.perform(delete("/api/v1/usuarios/" + userId))
                .andExpect(status().isNoContent());

        // User must be gone
        assertThat(usuarioRepository.findById(userId)).isEmpty();

        // Audit records must also be removed via CascadeType.ALL
        List<RegistroAuditoria> after = registroAuditoriaRepository.findByFkIdUsuarioIdUsuario(userId);
        assertThat(after).isEmpty();
    }
}
