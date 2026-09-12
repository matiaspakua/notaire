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
@DisplayName("Gestion archive — verifies pending debt before and while archiving (CU16, RF-22, RF-37)")
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

    private Integer createBudget(Integer clientId, Float propertyAmount) throws Exception {
        String body = """
                {"number": %d, "date": "2026-01-01", "encabezado": "Budget Archive IT",
                 "status": "Pending", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount, clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
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

    private Integer createProcedureTypeConWorkflowHaciaArchivada(ManagementStatus statusInicial) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName("Workflow Archive IT " + System.nanoTime());
        definition.setActive(true);
        definition = workflowDefinitionRepository.save(definition);

        WorkflowNode originNode = new WorkflowNode();
        originNode.setWorkflowDefinition(definition);
        originNode.setManagementStatus(statusInicial);
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
        type.setName("Tramite Archive IT");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        type.setWorkflowDefinition(definition);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    private Integer createManagementWithBudget(Float propertyAmount) throws Exception {
        Integer clientId = createPerson("43" + (System.nanoTime() % 1000000));
        Integer notaryId = createPerson("44" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, propertyAmount);
        ManagementStatus statusInicial = createManagementStatus();
        Integer statusId = statusInicial.getIdManagementStatus();
        Integer typeProcedureId = createProcedureTypeConWorkflowHaciaArchivada(statusInicial);
        String body = """
                {"number": %d, "encabezado": "Management Archive IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted((int) (System.nanoTime() % 100000), budgetId, notaryId, statusId,
                typeProcedureId);
        MvcResult result = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();
    }

    @Test
    @DisplayName("Archiving a gestión with pending debt reflects the aggregate balance in the response")
    void shouldReportPendingDebtWhenArchivingManagementWithBalance() throws Exception {
        Integer managementId = createManagementWithBudget(5000.00f);

        mockMvc.perform(get("/api/v1/gestiones/" + managementId + "/saldo-pendiente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingBalance").value(5000.00));

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/archivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId))
                .andExpect(jsonPath("$.pendingBalance").value(5000.00))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(true));
    }

    @Test
    @DisplayName("Confirming archiving despite pending debt still archives the gestión")
    void shouldArchiveManagementEvenWhenPendingDebtExists() throws Exception {
        Integer managementId = createManagementWithBudget(3000.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getName())
                .isEqualTo("Archivada");
    }

    @Test
    @DisplayName("Archiving a gestión with no pending debt reports zero balance")
    void shouldArchiveManagementWithoutDebtWarning() throws Exception {
        Integer managementId = createManagementWithBudget(0.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/archivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingBalance").value(0.00))
                .andExpect(jsonPath("$.pendingDebtAtArchiving").value(false));
    }

    @Test
    @DisplayName("Archiving record reflects pending debt when the gestión had an outstanding balance")
    void shouldPersistDebtPendingTrueWhenBalanceIsPositive() throws Exception {
        Integer managementId = createManagementWithBudget(1234.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting("pendingDebtAtArchiving")
                .isEqualTo(true);
    }

    @Test
    @DisplayName("Archiving record reflects no pending debt when the gestión had a zero balance")
    void shouldPersistDebtPendingFalseWhenBalanceIsZero() throws Exception {
        Integer managementId = createManagementWithBudget(0.00f);

        mockMvc.perform(post("/api/v1/gestiones/" + managementId + "/archivar"))
                .andExpect(status().isOk());

        assertThat(deedManagementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting("pendingDebtAtArchiving")
                .isEqualTo(false);
    }

    @Test
    @DisplayName("Should return 404 when archiving a gestión that does not exist")
    void shouldReturn404WhenArchivingMissingManagement() throws Exception {
        mockMvc.perform(post("/api/v1/gestiones/999999/archivar"))
                .andExpect(status().isNotFound());
    }
}
