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
import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.AuditRecordRepository;
import com.licensis.notaire.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Usuario delete — user is actually removed from DB")
class UserDeleteControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should delete usuario and return 404 on subsequent GET")
    void shouldDeleteUserAndReturnNotFoundOnSubsequentGet() throws Exception {
        // Create a user to delete (using persona id=1 from V2 test data)
        String createBody = """
                {
                  "name": "to_be_deleted",
                  "password": "pass",
                  "type": "EMPLEADO",
                  "active": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUser").asInt();

        // Delete the user
        mockMvc.perform(delete("/api/v1/usuarios/" + id))
                .andExpect(status().isNoContent());

        // Verify the user is actually gone
        mockMvc.perform(get("/api/v1/usuarios/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent usuario")
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should remove deleted usuario from list response")
    void shouldNotIncludeDeletedUserInList() throws Exception {
        String createBody = """
                {
                  "name": "to_be_removed_from_list",
                  "password": "pass",
                  "type": "EMPLEADO",
                  "active": true
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUser").asInt();

        // Verify user exists in the list
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(jsonPath("$[?(@.idUser == " + id + ")]").exists());

        // Delete
        mockMvc.perform(delete("/api/v1/usuarios/" + id))
                .andExpect(status().isNoContent());

        // Verify user is not in the list anymore
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(jsonPath("$[?(@.idUser == " + id + ")]").doesNotExist());
    }

    @Test
    @DisplayName("Should delete usuario and cascade-remove associated audit records")
    void shouldDeleteUserWithAuditRecordsViaCascade() throws Exception {
        // Create user via API (sets fkIdPersona=1 from seeded test data)
        String createBody = """
                {
                  "name": "user_with_audit",
                  "password": "pass",
                  "type": "EMPLEADO",
                  "active": true
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer userId = mapper.readTree(result.getResponse().getContentAsString())
                .get("idUser").asInt();

        // Simulate a prior operation — create audit record referencing this user
        User savedUser = userRepository.findById(userId).orElseThrow();
        AuditRecord audit = new AuditRecord();
        audit.setDate(new Date());
        audit.setModule("Usuarios");
        audit.setOperationDetail("POST");
        audit.setFkIdUser(savedUser);
        auditRecordRepository.save(audit);

        // Verify audit record exists before delete
        List<AuditRecord> before = auditRecordRepository.findByFkIdUserIdUser(userId);
        assertThat(before).isNotEmpty();

        // Delete user via API endpoint
        mockMvc.perform(delete("/api/v1/usuarios/" + userId))
                .andExpect(status().isNoContent());

        // User must be gone
        assertThat(userRepository.findById(userId)).isEmpty();

        // Audit records must also be removed via CascadeType.ALL
        List<AuditRecord> after = auditRecordRepository.findByFkIdUserIdUser(userId);
        assertThat(after).isEmpty();
    }
}
