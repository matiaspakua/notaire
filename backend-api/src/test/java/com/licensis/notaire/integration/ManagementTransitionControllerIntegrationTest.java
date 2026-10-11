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
 * Integration tests for POST /api/v1/managements/{id}/transition.
 * Seeds a workflow (INITIAL -&gt; INTERMEDIATE, no transition to a disconnected
 * FINAL node) and asserts the endpoint applies valid transitions and rejects
 * invalid ones, per CU83.
 */
@RequirementCoverage({"CU83"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("POST /managements/{id}/transition — validates against the workflow definition (CU83)")
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
    private ManagementStatus initialStatus;
    private ManagementStatus intermediateStatus;
    private ManagementStatus unreachableStatus;
    private Integer managementId;
    private Integer managementWithoutProcedureId;

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
        initialStatus = managementStatus("Initial transition");
        intermediateStatus = managementStatus("Intermediate transition");
        unreachableStatus = managementStatus("Unreachable transition");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName("Workflow transition IT " + System.nanoTime());
        definition.setActive(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode initialNode = new WorkflowNode();
        initialNode.setWorkflowDefinition(definition);
        initialNode.setManagementStatus(initialStatus);
        initialNode.setType(WorkflowNodeType.INITIAL);
        initialNode = workflowNodeRepository.save(initialNode);

        WorkflowNode intermediateNode = new WorkflowNode();
        intermediateNode.setWorkflowDefinition(definition);
        intermediateNode.setManagementStatus(intermediateStatus);
        intermediateNode.setType(WorkflowNodeType.FINAL);
        workflowNodeRepository.save(intermediateNode);

        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(definition);
        transition.setOriginNode(initialNode);
        transition.setDestinationNode(intermediateNode);
        workflowTransitionRepository.save(transition);

        ProcedureType type = new ProcedureType();
        type.setName("Transition type IT");
        type.setWorkflowDefinition(definition);
        type = procedureTypeRepository.save(type);

        managementId = createManagement(initialStatus, type, true);
        managementWithoutProcedureId = createManagement(initialStatus, type, false);

        entityManager.flush();
        entityManager.clear();
    }

    private Integer createManagement(ManagementStatus status, ProcedureType type, boolean withProcedure) {
        DeedManagement management = new DeedManagement();
        management.setNumber((int) (System.nanoTime() % 100000));
        management.setHeading("Transition management IT");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(status);
        management.setFkIdNotaryPerson(personRepository.findAll().get(0));
        management = managementRepository.save(management);

        if (withProcedure) {
            Procedure procedure = new Procedure();
            procedure.setFkIdManagement(management);
            procedure.setFkIdProcedureType(type);
            procedureRepository.save(procedure);
        }
        return management.getIdManagement();
    }

    @Test
    @DisplayName("Valid transition is applied")
    void shouldApplyValidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/managements/{id}/transition", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + intermediateStatus.getName() + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStatus").value(intermediateStatus.getName()));

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo(intermediateStatus.getName());
        assertThat(historyRepository.findByFkIdManagementIdManagement(managementId)).isNotEmpty();
    }

    @Test
    @DisplayName("Invalid transition is rejected")
    void shouldRejectInvalidTransition() throws Exception {
        mockMvc.perform(post("/api/v1/managements/{id}/transition", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + unreachableStatus.getName() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo(initialStatus.getName());
    }

    @Test
    @DisplayName("Management without a workflow rejects any transition")
    void shouldRejectTransitionWhenNoWorkflowDefinition() throws Exception {
        mockMvc.perform(post("/api/v1/managements/{id}/transition", managementWithoutProcedureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + intermediateStatus.getName() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 when transitioning a management that does not exist")
    void shouldReturn404WhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/v1/managements/999999/transition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + intermediateStatus.getName() + "\"}"))
                .andExpect(status().isNotFound());
    }
}
