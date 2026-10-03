package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.testing.RequirementCoverage;

import jakarta.persistence.EntityManager;

/**
 * #804 — Enforce WorkflowDefinition on orphan status write paths (CU83).
 * Illegal PUT / complete-case status mutations must be rejected; create must
 * validate initial status against workflow nodes when a workflow exists.
 */
@RequirementCoverage({"CU83", "CU02", "CU53"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("CU83 — enforce workflow on gestión status writes (#804)")
class ManagementWorkflowStatusWriteEnforcementIntegrationTest {

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
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    private ManagementStatus startStatus;
    private ManagementStatus nextStatus;
    private ManagementStatus outsideWorkflowStatus;
    private Integer procedureTypeId;
    private Integer startNodeId;
    private Integer nextNodeId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedWorkflow();
    }

    private ManagementStatus createStatus(String prefix) {
        ManagementStatus status = new ManagementStatus();
        status.setName(prefix + " " + System.nanoTime());
        return statusRepository.save(status);
    }

    private void seedWorkflow() {
        startStatus = createStatus("Start");
        nextStatus = createStatus("Next");
        outsideWorkflowStatus = createStatus("Outside");

        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName("Enforce Workflow IT " + System.nanoTime());
        definition.setActive(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode startNode = new WorkflowNode();
        startNode.setWorkflowDefinition(definition);
        startNode.setManagementStatus(startStatus);
        startNode.setType(WorkflowNodeType.INITIAL);
        startNode = workflowNodeRepository.save(startNode);
        startNodeId = startNode.getId();

        WorkflowNode nextNode = new WorkflowNode();
        nextNode.setWorkflowDefinition(definition);
        nextNode.setManagementStatus(nextStatus);
        nextNode.setType(WorkflowNodeType.FINAL);
        nextNode = workflowNodeRepository.save(nextNode);
        nextNodeId = nextNode.getId();

        WorkflowTransition transition = new WorkflowTransition();
        transition.setWorkflowDefinition(definition);
        transition.setOriginNode(startNode);
        transition.setDestinationNode(nextNode);
        workflowTransitionRepository.save(transition);

        ProcedureType type = new ProcedureType();
        type.setName("Type Enforce IT " + System.nanoTime());
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type.setWorkflowDefinition(definition);
        procedureTypeId = procedureTypeRepository.save(type).getIdProcedureType();

        entityManager.flush();
        entityManager.clear();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary", "lastName": "EnforceIT", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createBudget(Integer clientId) throws Exception {
        String body = """
                {"number": 1, "date": "2026-01-01", "encabezado": "Budget Enforce IT", "status": "Pending",
                 "person": {"personId": %d}}
                """.formatted(clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createCompleteCase(Integer budgetId, Integer notaryId, Integer statusId, int number)
            throws Exception {
        String body = """
                {"number": %d, "encabezado": "Enforce IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, statusId, procedureTypeId);
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();
        // Controllers save Procedure via FK without updating DeedManagement.procedureList;
        // clear the persistence context so later findById reloads the collection.
        entityManager.flush();
        entityManager.clear();
        return managementId;
    }

    @Test
    @DisplayName("Plain PUT that changes status is rejected")
    void shouldRejectPlainPutThatChangesStatus() throws Exception {
        Integer notaryId = createPerson("80410001");
        int number = (int) (System.nanoTime() % 100000);
        String createBody = """
                {"encabezado": "Plain PUT reject", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, startStatus.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        String updateBody = """
                {"encabezado": "Plain PUT reject", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, nextStatus.getIdManagementStatus());
        mockMvc.perform(put("/api/v1/gestiones/{id}", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("transition")));

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(startStatus.getIdManagementStatus());
    }

    @Test
    @DisplayName("Complete-case PUT that changes status is rejected")
    void shouldRejectCompleteCasePutThatChangesStatus() throws Exception {
        Integer clientId = createPerson("80410002");
        Integer notaryId = createPerson("80410003");
        Integer budgetId = createBudget(clientId);
        int number = (int) (System.nanoTime() % 100000);
        Integer managementId = createCompleteCase(budgetId, notaryId,
                startStatus.getIdManagementStatus(), number);

        String updateBody = """
                {"number": %d, "encabezado": "Enforce IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, nextStatus.getIdManagementStatus(),
                        procedureTypeId);
        mockMvc.perform(put("/api/v1/gestiones/{id}/complete-case", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("transition")));

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(startStatus.getIdManagementStatus());
    }

    @Test
    @DisplayName("Plain PUT that keeps the same status succeeds")
    void shouldAllowPlainPutThatKeepsSameStatus() throws Exception {
        Integer notaryId = createPerson("80410004");
        int number = (int) (System.nanoTime() % 100000);
        String createBody = """
                {"encabezado": "Same status PUT", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, startStatus.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        String updateBody = """
                {"encabezado": "Same status PUT renamed", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, startStatus.getIdManagementStatus());
        mockMvc.perform(put("/api/v1/gestiones/{id}", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        DeedManagement saved = managementRepository.findById(managementId).orElseThrow();
        assertThat(saved.getEncabezado()).isEqualTo("Same status PUT renamed");
        assertThat(saved.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(startStatus.getIdManagementStatus());
    }

    @Test
    @DisplayName("Complete-case create with start-node status succeeds")
    void shouldAllowCompleteCaseCreateWithStartNodeStatus() throws Exception {
        Integer clientId = createPerson("80410005");
        Integer notaryId = createPerson("80410006");
        Integer budgetId = createBudget(clientId);
        int number = (int) (System.nanoTime() % 100000);

        Integer managementId = createCompleteCase(budgetId, notaryId,
                startStatus.getIdManagementStatus(), number);

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(startStatus.getIdManagementStatus());
    }

    @Test
    @DisplayName("Complete-case create with status outside the workflow is rejected")
    void shouldRejectCompleteCaseCreateWithStatusOutsideWorkflow() throws Exception {
        Integer clientId = createPerson("80410007");
        Integer notaryId = createPerson("80410008");
        Integer budgetId = createBudget(clientId);
        int number = (int) (System.nanoTime() % 100000);

        String body = """
                {"number": %d, "encabezado": "Outside workflow", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId,
                        outsideWorkflowStatus.getIdManagementStatus(), procedureTypeId);
        mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Workflow-trace lists transitions usable as legal next states")
    void shouldExposeLegalNextDestinationsViaWorkflowTrace() throws Exception {
        Integer clientId = createPerson("80410009");
        Integer notaryId = createPerson("80410010");
        Integer budgetId = createBudget(clientId);
        int number = (int) (System.nanoTime() % 100000);
        Integer managementId = createCompleteCase(budgetId, notaryId,
                startStatus.getIdManagementStatus(), number);

        MvcResult result = mockMvc.perform(get("/api/v1/gestiones/{id}/workflow-trace", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusActual").value(startStatus.getName()))
                .andExpect(jsonPath("$.transitions").isArray())
                .andReturn();

        JsonNode root = mapper.readTree(result.getResponse().getContentAsString());
        boolean hasOutbound = false;
        for (JsonNode transition : root.get("transitions")) {
            if (transition.get("originNodeId").asInt() == startNodeId
                    && transition.get("destinationNodeId").asInt() == nextNodeId) {
                hasOutbound = true;
                break;
            }
        }
        assertThat(hasOutbound)
                .as("workflow-trace must include the outbound edge from the current node")
                .isTrue();
    }

    @Test
    @DisplayName("Legal transition via POST /transition still succeeds after enforcement")
    void shouldStillAllowLegalTransitionEndpoint() throws Exception {
        Integer clientId = createPerson("80410011");
        Integer notaryId = createPerson("80410012");
        Integer budgetId = createBudget(clientId);
        int number = (int) (System.nanoTime() % 100000);
        Integer managementId = createCompleteCase(budgetId, notaryId,
                startStatus.getIdManagementStatus(), number);

        mockMvc.perform(post("/api/v1/gestiones/{id}/transition", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusDestination\": \"" + nextStatus.getName() + "\"}"))
                .andExpect(status().isOk());

        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(nextStatus.getIdManagementStatus());
    }
}
