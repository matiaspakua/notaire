package com.licensis.notaire.integration;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GET /api/v1/gestiones/{id}/workflow-trace.
 * Seeds a workflow (INITIAL → INTERMEDIATE → FINAL), a gestión whose trámite
 * uses that workflow's tipo, and historial entries covering the first two
 * estados, then asserts the aggregated trace and computed node statuses.
 */
@RequirementCoverage({"CU83"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("CU83 — Workflow trace endpoint integration tests (H2)")
class WorkflowTraceApiH2IntegrationTest {

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
    private Integer managementId;
    private Integer initialNodeId;
    private Integer middleNodeId;
    private Integer finalNodeId;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedWorkflowAndManagement();
    }

    private ManagementStatus managementStatus(String name) {
        ManagementStatus status = new ManagementStatus();
        status.setName(name);
        return statusRepository.save(status);
    }

    private WorkflowNode node(WorkflowDefinition def, ManagementStatus status, WorkflowNodeType type) {
        WorkflowNode node = new WorkflowNode();
        node.setWorkflowDefinition(def);
        node.setManagementStatus(status);
        node.setType(type);
        return workflowNodeRepository.save(node);
    }

    private History history(DeedManagement management, ManagementStatus status, long epochMillis) {
        History entry = new History();
        entry.setFkIdManagement(management);
        entry.setFkIdManagementStatus(status);
        entry.setDate(new Date(epochMillis));
        return historyRepository.save(entry);
    }

    private void seedWorkflowAndManagement() {
        ManagementStatus iniciada = managementStatus("Trace Iniciada");
        ManagementStatus enCurso = managementStatus("Trace En Curso");
        ManagementStatus cerrada = managementStatus("Trace Cerrada");

        WorkflowDefinition def = new WorkflowDefinition();
        def.setName("Workflow Trace Test");
        def.setActive(true);
        def = workflowDefinitionRepository.save(def);

        WorkflowNode initial = node(def, iniciada, WorkflowNodeType.INITIAL);
        WorkflowNode middle = node(def, enCurso, WorkflowNodeType.INTERMEDIATE);
        WorkflowNode last = node(def, cerrada, WorkflowNodeType.FINAL);
        initialNodeId = initial.getId();
        middleNodeId = middle.getId();
        finalNodeId = last.getId();

        WorkflowTransition t1 = new WorkflowTransition();
        t1.setWorkflowDefinition(def);
        t1.setOriginNode(initial);
        t1.setDestinationNode(middle);
        workflowTransitionRepository.save(t1);
        WorkflowTransition t2 = new WorkflowTransition();
        t2.setWorkflowDefinition(def);
        t2.setOriginNode(middle);
        t2.setDestinationNode(last);
        workflowTransitionRepository.save(t2);

        ProcedureType type = new ProcedureType();
        type.setName("Tipo Trace Test");
        type.setWorkflowDefinition(def);
        type = procedureTypeRepository.save(type);

        DeedManagement management = new DeedManagement();
        management.setIdManagement(null);
        management.setNumber(987654);
        management.setEncabezado("Gestion Trace Test");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(enCurso);
        management.setFkIdNotaryPerson(personRepository.findAll().get(0));
        management = managementRepository.save(management);
        managementId = management.getIdManagement();

        Procedure procedure = new Procedure();
        procedure.setIdProcedure(null);
        procedure.setFkIdManagement(management);
        procedure.setFkIdProcedureType(type);
        procedureRepository.save(procedure);

        long oneDayMillis = 86_400_000L;
        history(management, iniciada, oneDayMillis);
        history(management, enCurso, 2 * oneDayMillis);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should return aggregated trace with nodes, transitions and history")
    void shouldReturnAggregatedTraceWithNodesTransitionsAndHistory() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(managementId))
                .andExpect(jsonPath("$.number").value(987654))
                .andExpect(jsonPath("$.encabezado").value("Gestion Trace Test"))
                .andExpect(jsonPath("$.statusActual").value("Trace En Curso"))
                .andExpect(jsonPath("$.workflowDefinition.name").value("Workflow Trace Test"))
                .andExpect(jsonPath("$.nodes", hasSize(3)))
                .andExpect(jsonPath("$.transitions", hasSize(2)))
                .andExpect(jsonPath("$.history", hasSize(2)));
    }

    @Test
    @DisplayName("Should compute completed, in_progress and pending node statuses")
    void shouldComputeNodeStatusesFromHistory() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeStatuses." + initialNodeId).value("completed"))
                .andExpect(jsonPath("$.nodeStatuses." + middleNodeId).value("in_progress"))
                .andExpect(jsonPath("$.nodeStatuses." + finalNodeId).value("pending"));
    }

    @Test
    @DisplayName("Should return 400 when gestion does not exist")
    void shouldReturnBadRequestWhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", 999999))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should serialize gestiones list when tramite type has a workflow definition")
    void shouldSerializeGestionesListWhenTypeHasWorkflowDefinition() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idManagement").value(managementId))
                .andExpect(jsonPath("$.content[0].procedureCount").value(1));
    }

    @Test
    @DisplayName("Should serialize gestion by number when tramite type has a workflow definition")
    void shouldSerializeManagementByNumberWhenTypeHasWorkflowDefinition() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/numero/{numero}", 987654))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(987654))
                .andExpect(jsonPath("$.statusActual").value("Trace En Curso"))
                .andExpect(jsonPath("$.procedureCount").value(1));
    }

    @Test
    @DisplayName("Should serialize type-tramite list when a workflow definition is assigned")
    void shouldSerializeTypeProcedureListWhenWorkflowDefinitionAssigned() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-tramite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Tipo Trace Test')].workflowDefinitionName")
                        .value("Workflow Trace Test"));
    }

    @Test
    @DisplayName("Should serialize history endpoints without entity cycles")
    void shouldSerializeHistoryEndpointsWithoutEntityCycles() throws Exception {
        mockMvc.perform(get("/api/v1/historial"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/historial/gestion/{id}", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].managementId").value(managementId));
        mockMvc.perform(get("/api/v1/gestiones/{id}/estado-actual", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusManagementName").value("Trace En Curso"));
    }
}
