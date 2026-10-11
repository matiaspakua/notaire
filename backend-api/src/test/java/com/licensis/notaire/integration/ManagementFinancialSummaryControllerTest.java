package com.licensis.notaire.integration;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;
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

@RequirementCoverage({"CU47", "CU02"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Management financial summary — CU47/CU02 aggregate financial summary endpoint")
class ManagementFinancialSummaryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ManagementStatusRepository managementStatusRepository;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary IT", "lastName": "Summary Management IT", "identificationNumber": "%s",
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
                {"number": %d, "date": "2026-01-01", "heading": "Budget Summary Management IT",
                 "status": "PENDIENTE", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount, clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createManagementStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Status Summary Management IT");
        return managementStatusRepository.save(status).getIdManagementStatus();
    }

    private Integer createProcedureType() {
        ProcedureType type = new ProcedureType();
        type.setName("Procedure Summary Management IT");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    private Integer createManagementWithBudget(Integer budgetId) throws Exception {
        Integer notaryId = createPerson("64" + (System.nanoTime() % 1000000));
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        String body = """
                {"number": %d, "heading": "Management Summary IT", "budgetId": %d,
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

    private void createPayment(Integer idBudget, java.math.BigDecimal amount) throws Exception {
        String body = """
                {"idBudget": %d, "amount": %s, "date": "2026-08-20", "notes": "Payment Summary Management IT"}
                """.formatted(idBudget, amount);
        mockMvc.perform(post("/api/v1/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return the aggregate financial summary for a management with a payment")
    void shouldReturnFinancialSummaryForManagement() throws Exception {
        Integer clientId = createPerson("65" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, new java.math.BigDecimal("5000.00"));
        createPayment(budgetId, new java.math.BigDecimal("2000.00"));
        Integer managementId = createManagementWithBudget(budgetId);

        mockMvc.perform(get("/api/v1/managements/" + managementId + "/financial-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId))
                .andExpect(jsonPath("$.totalBudgeted").value(5000.00))
                .andExpect(jsonPath("$.totalCollected").value(2000.00))
                .andExpect(jsonPath("$.pendingBalance").value(3000.00));
    }

    @Test
    @DisplayName("Should return 404 when requesting the financial summary of a non-existent management")
    void shouldReturnNotFoundForUnknownManagement() throws Exception {
        mockMvc.perform(get("/api/v1/managements/999999/financial-summary"))
                .andExpect(status().isNotFound());
    }
}
