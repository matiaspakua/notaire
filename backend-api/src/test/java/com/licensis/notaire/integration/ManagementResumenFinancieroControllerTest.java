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
@DisplayName("Gestion resumen financiero — CU47/CU02 aggregate financial summary endpoint")
class ManagementResumenFinancieroControllerTest {

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
                {"firstName": "Notary IT", "lastName": "Resumen Management IT", "identificationNumber": "%s",
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
                {"number": %d, "date": "2026-01-01", "encabezado": "Budget Resumen Management IT",
                 "status": "Pending", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount, clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createManagementStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Estado Resumen Gestion IT");
        return managementStatusRepository.save(status).getIdManagementStatus();
    }

    private Integer createProcedureType() {
        ProcedureType type = new ProcedureType();
        type.setName("Tramite Resumen Gestion IT");
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
                {"number": %d, "encabezado": "Management Resumen IT", "budgetId": %d,
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

    private void createPayment(Integer idBudget, Float amount) throws Exception {
        String body = """
                {"idBudget": %d, "amount": %s, "date": "2026-08-20", "notes": "Payment Resumen Management IT"}
                """.formatted(idBudget, amount);
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return the aggregate financial summary for a gestión with a payment")
    void shouldReturnResumenFinancieroForManagement() throws Exception {
        Integer clientId = createPerson("65" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, 5000.00f);
        createPayment(budgetId, 2000.00f);
        Integer managementId = createManagementWithBudget(budgetId);

        mockMvc.perform(get("/api/v1/gestiones/" + managementId + "/resumen-financiero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId))
                .andExpect(jsonPath("$.totalPresupuestado").value(5000.00))
                .andExpect(jsonPath("$.totalCobrado").value(2000.00))
                .andExpect(jsonPath("$.pendingBalance").value(3000.00));
    }

    @Test
    @DisplayName("Should return 404 when requesting the financial summary of a non-existent gestión")
    void shouldReturnNotFoundForUnknownManagement() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/resumen-financiero"))
                .andExpect(status().isNotFound());
    }
}
