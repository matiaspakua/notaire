package com.licensis.notaire.integration;

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
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;

/**
 * Issue #1360: the management edit dialog needs the current case values (budget, notary,
 * status, procedure type, property) to prefill a full edit through PUT /managements/{id}/complete-case.
 * GET /managements/{id}/complete-case returns them in the same shape the PUT accepts.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU02", "CU53"})
@DisplayName("Management complete-case read (#1360) — the edit form gets the values the PUT expects")
class ManagementCompleteCaseReadIntegrationTest {

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
                {"firstName": "Notary 1360", "lastName": "Case Read", "identificationNumber": "%s",
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
                {"number": 1, "date": "2026-01-01", "heading": "Budget 1360", "status": "PENDIENTE",
                 "person": {"personId": %d}}
                """.formatted(clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createManagementStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Status 1360");
        return managementStatusRepository.save(status).getIdManagementStatus();
    }

    private Integer createProcedureType() {
        ProcedureType type = new ProcedureType();
        type.setName("Procedure 1360");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    private Integer createCase(int number, Integer budgetId, Integer notaryId, Integer statusId,
            Integer typeProcedureId) throws Exception {
        String body = """
                {"number": %d, "heading": "Case 1360", "notes": "keep me", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, statusId, typeProcedureId);
        MvcResult created = mockMvc.perform(post("/api/v1/managements/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(created.getResponse().getContentAsString()).get("idManagement").asInt();
    }

    @Test
    @DisplayName("GET complete-case returns the ids the PUT accepts")
    void returnsTheCaseValues() throws Exception {
        Integer clientId = createPerson("13600001");
        Integer notaryId = createPerson("13600002");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        Integer managementId = createCase(13601, budgetId, notaryId, statusId, typeProcedureId);

        mockMvc.perform(get("/api/v1/managements/{id}/complete-case", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId))
                .andExpect(jsonPath("$.number").value(13601))
                .andExpect(jsonPath("$.heading").value("Case 1360"))
                .andExpect(jsonPath("$.notes").value("keep me"))
                .andExpect(jsonPath("$.budgetId").value(budgetId))
                .andExpect(jsonPath("$.notaryId").value(notaryId))
                .andExpect(jsonPath("$.statusManagementId").value(statusId))
                .andExpect(jsonPath("$.typeProcedureId").value(typeProcedureId))
                .andExpect(jsonPath("$.propertyId").doesNotExist());
    }

    @Test
    @DisplayName("GET complete-case answers 404 for an unknown management")
    void unknownManagementIs404() throws Exception {
        mockMvc.perform(get("/api/v1/managements/{id}/complete-case", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A notary change saved through PUT complete-case reads back, notes kept")
    void notaryChangeReadsBack() throws Exception {
        Integer clientId = createPerson("13600003");
        Integer notaryId = createPerson("13600004");
        Integer otherNotaryId = createPerson("13600005");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        Integer managementId = createCase(13602, budgetId, notaryId, statusId, typeProcedureId);

        String update = """
                {"number": 13602, "heading": "Case 1360", "notes": "keep me", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, otherNotaryId, statusId, typeProcedureId);
        mockMvc.perform(put("/api/v1/managements/{id}/complete-case", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/managements/{id}/complete-case", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notaryId").value(otherNotaryId))
                .andExpect(jsonPath("$.notes").value("keep me"));
    }
}
