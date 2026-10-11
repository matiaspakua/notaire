package com.licensis.notaire.integration;


import static org.assertj.core.api.Assertions.assertThat;
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
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.WorkflowDefinition;
import com.licensis.notaire.business.WorkflowNode;
import com.licensis.notaire.business.WorkflowNodeType;
import com.licensis.notaire.business.WorkflowTransition;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.WorkflowDefinitionRepository;
import com.licensis.notaire.repository.WorkflowNodeRepository;
import com.licensis.notaire.repository.WorkflowTransitionRepository;
import com.licensis.notaire.testing.RequirementCoverage;

@RequirementCoverage({"CU16", "RF-22", "RF-37"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Management archive — verifies pending debt before and while archiving (CU16, RF-22, RF-37)")
class ManagementArchiveIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ManagementStatusRepository managementStatusRepository;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private DeedManagementRepository deedManagementRepository;

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Autowired
    private WorkflowNodeRepository workflowNodeRepository;

    @Autowired
    private WorkflowTransitionRepository workflowTransitionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private ManagementStatus archivada;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        archivada = managementStatusRepository.findByName("Archivada").orElseGet(() -> {
            ManagementStatus nuevo = new ManagementStatus();
            nuevo.setName("Archivada");
            return managementStatusRepository.save(nuevo);
        });
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary IT", "lastName": "Archive IT", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createBudget(Integer clientId, java.math.BigDecimal propertyAmount) throws Exception {
        String body = """
                {"number": %d, "date": "2026-01-01", "heading": "Budget Archive IT",
                 "status": "PENDIENTE", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount, clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private ManagementStatus createManagementStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Estado Archive IT " + System.nanoTime());
        return managementStatusRepository.save(status);
    }

    private Integer createProcedureTypeConWorkflowHaciaArchivada(ManagementStatus initialStatus) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName("Workflow Archive IT " + System.nanoTime());
        definition.setActive(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode originNode = new WorkflowNode();
        originNode.setWorkflowDefinition(definition);
        originNode.setManagementStatus(initialStatus);
        originNode.setType(WorkflowNodeType.INITIAL);
        originNode = workflowNodeRepository.save(originNode);

        WorkflowNode destinationNode = new WorkflowNode();
        destinationNode.setWorkflowDefinition(definition);
        destinationNode.setManagementStatus(archivada);
        destinationNode.setType(WorkflowNodeType.FINAL);
        destinationNode = workflowNodeRepository.save(destinationNode);

        WorkflowTransition transicion = new WorkflowTransition();
        transicion.setWorkflowDefinition(definition);
        transicion.setOriginNode(originNode);
        transicion.setDestinationNode(destinationNode);
        workflowTransitionRepository.save(transicion);

        ProcedureType type = new ProcedureType();
        type.setName("Procedure Archive IT");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type.setWorkflowDefinition(definition);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    private Integer createManagementWithBudget(java.math.BigDecimal propertyAmount) throws Exception {
        Integer clientId = createPerson("43" + (System.nanoTime() % 1000000));
        Integer notaryId = createPerson("44" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, propertyAmount);
        ManagementStatus initialStatus = createManagementStatus();
        Integer statusId = initialStatus.getIdManagementStatus();
        Integer typeProcedureId = createProcedureTypeConWorkflowHaciaArchivada(initialStatus);
        String body = """
                {"number": %d, "heading": "Management Archive IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted((int) (System.nanoTime() % 100000), budgetId, notaryId, statusId,
                typeProcedureId);
        MvcResult result = mockMvc.perform(post("/api/v1/managements/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();
    }

    @Test
    @DisplayName("Archiving a management with pending debt reflects the aggregate balance in the response")
    void shouldReportPendingDebtWhenArchivingManagementWithBalance() throws Exception {
        Integer managementId = createManagementWithBudget(new java.math.BigDecimal("5000.00"));

        mockMvc.perform(get("/api/v1/managements/" + managementId + "/outstanding-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingBalance").value(5000.00));

        mockMvc.perform(post("/api/v1/managements/" + managementId + "/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId))
                .andExpect(jsonPath("$.pendingBalance").value(5000.00))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(true));
    }

    @Test
    @DisplayName("Confirming archiving despite pending debt still archives the management")
    void shouldArchiveManagementEvenWhenPendingDebtExists() throws Exception {
        Integer managementId = createManagementWithBudget(new java.math.BigDecimal("3000.00"));

        mockMvc.perform(post("/api/v1/managements/" + managementId + "/archive"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo("Archivada");
    }

    @Test
    @DisplayName("Archiving a management with not pending debt reports zero balance")
    void shouldArchiveManagementWithoutDebtWarning() throws Exception {
        Integer managementId = createManagementWithBudget(new java.math.BigDecimal("0.00"));

        mockMvc.perform(post("/api/v1/managements/" + managementId + "/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingBalance").value(0.00))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(false));
    }

    @Test
    @DisplayName("Archiving record reflects pending debt when the management had an outstanding balance")
    void shouldPersistDebtPendingTrueWhenBalanceIsPositive() throws Exception {
        Integer managementId = createManagementWithBudget(new java.math.BigDecimal("1234.00"));

        mockMvc.perform(post("/api/v1/managements/" + managementId + "/archive"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting("pendingDebtAtArchiving")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("Archiving record reflects not pending debt when the management had a zero balance")
    void shouldPersistDebtPendingFalseWhenBalanceIsZero() throws Exception {
        Integer managementId = createManagementWithBudget(new java.math.BigDecimal("0.00"));

        mockMvc.perform(post("/api/v1/managements/" + managementId + "/archive"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting("pendingDebtAtArchiving")
                .isEqualTo(false);
    }

    @Test
    @DisplayName("Should return 404 when archiving a management that does not exist")
    void shouldReturn404WhenArchivingMissingManagement() throws Exception {
        mockMvc.perform(post("/api/v1/managements/999999/archive"))
                .andExpect(status().isNotFound());
    }
}
