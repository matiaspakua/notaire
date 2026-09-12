package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("Presupuesto resumen — CU47 financial summary endpoint")
class BudgetResumenControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Client IT", "lastName": "Resumen IT", "identificationNumber": "%s",
                 "isClient": true, "identificationType": {"idIdentificationType": 1}}
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
                {"number": %d, "date": "2026-01-01", "encabezado": "Budget Resumen IT",
                 "status": "Pending", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount, clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private void createPayment(Integer idBudget, Float amount) throws Exception {
        String body = """
                {"idBudget": %d, "amount": %s, "date": "2026-08-20", "notes": "Payment Resumen IT"}
                """.formatted(idBudget, amount);
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return the financial summary for a budget with no payments")
    void shouldReturnSummaryForBudgetWithoutPayments() throws Exception {
        Integer clientId = createPerson("51" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, 5000.00f);

        mockMvc.perform(get("/api/v1/presupuestos/" + budgetId + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idBudget").value(budgetId))
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.pendingBalance").value(5000.00))
                .andExpect(jsonPath("$.payments").isEmpty());
    }

    @Test
    @DisplayName("Should return the financial summary reflecting a registered payment")
    void shouldReturnSummaryForBudgetWithPayment() throws Exception {
        Integer clientId = createPerson("52" + (System.nanoTime() % 1000000));
        Integer budgetId = createBudget(clientId, 5000.00f);
        createPayment(budgetId, 2000.00f);

        mockMvc.perform(get("/api/v1/presupuestos/" + budgetId + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5000.00))
                .andExpect(jsonPath("$.pendingBalance").value(3000.00))
                .andExpect(jsonPath("$.payments.length()").value(1));
    }

    @Test
    @DisplayName("Should return 404 when requesting the summary of a non-existent budget")
    void shouldReturnNotFoundForUnknownBudget() throws Exception {
        mockMvc.perform(get("/api/v1/presupuestos/999999/resumen"))
                .andExpect(status().isNotFound());
    }
}
