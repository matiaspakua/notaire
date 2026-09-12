package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.PersonRepository;

@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@Transactional
@DisplayName("CU28 — EstadoDeGestion referential integrity and search (issue #437)")
class ManagementStatusReferentialIntegrityTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DeedManagementRepository managementRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ManagementStatusRepository statusRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private int createStatus(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/estado-gestion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "%s", "notes": "Test"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idManagementStatus").asInt();
    }

    private void linkStatusToManagement(int idManagementStatus) {
        Person notary = personRepository.findById(1).orElseThrow();
        ManagementStatus status = statusRepository.findById(idManagementStatus).orElseThrow();

        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setNumber(90000 + idManagementStatus);
        management.setEncabezado("Test gestion for status " + idManagementStatus);
        management.setFkIdNotaryPerson(notary);
        management.setFkIdManagementStatus(status);
        managementRepository.save(management);
    }

    @Test
    @DisplayName("Should return inUse=false when no gestion references the status")
    void shouldReturnInUseFalseWhenStatusIsNotReferenced() throws Exception {
        int id = createStatus("EstadoFree_InUse");

        mockMvc.perform(get("/api/v1/estado-gestion/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(false));
    }

    @Test
    @DisplayName("Should return inUse=true when a gestion references the status")
    void shouldReturnInUseTrueWhenStatusIsReferenced() throws Exception {
        int id = createStatus("EstadoUsed_InUse");
        linkStatusToManagement(id);

        mockMvc.perform(get("/api/v1/estado-gestion/" + id + "/in-use"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inUse").value(true));
    }

    @Test
    @DisplayName("Should return 409 when editing an status that is in use")
    void shouldReturn409WhenEditingStatusInUse() throws Exception {
        int id = createStatus("EstadoEditBlocked");
        linkStatusToManagement(id);

        mockMvc.perform(put("/api/v1/estado-gestion/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "StatusEditBlockedUpdated", "notes": "Updated"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should allow editing an status that is NOT in use")
    void shouldAllowEditingStatusNotInUse() throws Exception {
        int id = createStatus("EstadoEditAllowed");

        mockMvc.perform(put("/api/v1/estado-gestion/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "StatusEditAllowedUpdated", "notes": "Updated"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 409 when deleting an status that is in use")
    void shouldReturn409WhenDeletingStatusInUse() throws Exception {
        int id = createStatus("EstadoDeleteBlocked");
        linkStatusToManagement(id);

        mockMvc.perform(delete("/api/v1/estado-gestion/" + id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should search estados by name")
    void shouldSearchEstadosByName() throws Exception {
        createStatus("BuscarEsteEstado");

        mockMvc.perform(get("/api/v1/estado-gestion/search").param("name", "BuscarEste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("BuscarEsteEstado"));
    }
}
