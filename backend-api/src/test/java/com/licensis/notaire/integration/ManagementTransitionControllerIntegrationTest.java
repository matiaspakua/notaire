package com.licensis.notaire.integration;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for POST /api/v1/gestiones/{id}/transition.
 * Seeds a workflow (INITIAL -&gt; INTERMEDIATE, no transition to a disconnected
 * FINAL node) and asserts the endpoint applies valid transitions and rejects
 * invalid ones, per CU83.
 */
@RequirementCoverage({"CU83"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("POST /gestiones/{id}/transition — validates against the workflow definition (CU83)")
class ManagementTransitionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;
    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;
    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private ProcedureRepository procedureRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private ManagementStatus statusInicial;
    private ManagementStatus statusIntermedio;
    private ManagementStatus statusInalcanzable;
    private Integer managementId;
    private Integer managementSinProcedureId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedWorkflowAndManagement();
    }

    private ManagementStatus managementStatus(String name) {
        ManagementStatus status = new ManagementStatus();
        status.setName(name + " " + System.nanoTime());
        return statusRepository.save(status);
    }

    private void seedWorkflowAndManagement() {
        statusInicial = managementStatus("Transicion Inicial");
        statusIntermedio = managementStatus("Transicion Intermedio");
        statusInalcanzable = managementStatus("Transicion Inalcanzable");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName("Workflow Transicion IT " + System.nanoTime());
        definition.setActive(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode nodeInicial = new WorkflowNode();
        nodeInicial.setWorkflowDefinition(definition);
        nodeInicial.setManagementStatus(statusInicial);
        nodeInicial.setType(WorkflowNodeType.INITIAL);
        nodeInicial = workflowNodeRepository.save(nodeInicial);

        WorkflowNode nodeIntermedio = new WorkflowNode();
        nodeIntermedio.setWorkflowDefinition(definition);
        nodeIntermedio.setManagementStatus(statusIntermedio);
        nodeIntermedio.setType(WorkflowNodeType.FINAL);
        workflowNodeRepository.save(nodeIntermedio);

        WorkflowTransition transicion = new WorkflowTransition();
        transicion.setWorkflowDefinition(definition);
        transicion.setOriginNode(nodeInicial);
        transicion.setDestinationNode(nodeIntermedio);
        workflowTransitionRepository.save(transicion);

        ProcedureType type = new ProcedureType();
        type.setName("Tipo Transicion IT");
        type.setWorkflowDefinition(definition);
        type = procedureTypeRepository.save(type);

        managementId = createManagement(statusInicial, type, true);
        managementSinProcedureId = createManagement(statusInicial, type, false);

        entityManager.flush();
        entityManager.clear();
    }

    private Integer createManagement(ManagementStatus status, ProcedureType type, boolean conProcedure) {
        DeedManagement management = new DeedManagement();
        management.setNumber((int) (System.nanoTime() % 100000));
        management.setEncabezado("Gestion Transicion IT");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(status);
        management.setFkIdNotaryPerson(personRepository.findAll().get(0));
        management = managementRepository.save(management);

        if (conProcedure) {
            Procedure procedure = new Procedure();
            procedure.setFkIdManagement(management);
            procedure.setFkIdProcedureType(type);
            procedureRepository.save(procedure);
        }
        return management.getIdManagement();
    }

    @Test
    @DisplayName("Transición válida se aplica")
    void shouldApplyValidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transition", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + statusIntermedio.getName() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusActual").value(statusIntermedio.getName()));

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo(statusIntermedio.getName());
        assertThat(historyRepository.findByFkIdManagementIdManagement(managementId)).isNotEmpty();
    }

    @Test
    @DisplayName("Transición inválida es rechazada")
    void shouldRejectInvalidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transition", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + statusInalcanzable.getName() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo(statusInicial.getName());
    }

    @Test
    @DisplayName("Gestión sin workflow definido rechaza cualquier transición")
    void shouldRejectTransitionWhenNoWorkflowDefinition() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/{id}/transition", managementSinProcedureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + statusIntermedio.getName() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 when transitioning a gestión that does not exist")
    void shouldReturn404WhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/999999/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + statusIntermedio.getName() + "\"}"))
                .andExpect(status().isNotFound());
    }
}
