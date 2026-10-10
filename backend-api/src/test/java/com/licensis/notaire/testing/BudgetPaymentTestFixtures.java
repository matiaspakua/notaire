package com.licensis.notaire.testing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Shared H2 IT helpers for arranging person + presupuesto fixtures used by payment
 * mutation tests (Issue #916). Prefer these over hardcoding seed {@code idBudget=1}.
 */
public final class BudgetPaymentTestFixtures {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private BudgetPaymentTestFixtures() {
    }

    public static Integer createPerson(MockMvc mockMvc, String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Payment IT", "lastName": "Fixture", "identificationNumber": "%s",
                 "isClient": true, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return MAPPER.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    public static Integer createBudget(MockMvc mockMvc, Integer clientId, BigDecimal propertyAmount)
            throws Exception {
        String body = """
                {"number": %d, "date": "2026-01-01", "encabezado": "Budget Payment Fixture IT",
                 "status": "PENDIENTE", "propertyAmount": %s, "person": {"personId": %d}}
                """.formatted((int) (System.nanoTime() % 100000), propertyAmount.toPlainString(), clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return MAPPER.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    public static Integer createIsolatedBudget(MockMvc mockMvc, BigDecimal propertyAmount) throws Exception {
        Integer clientId = createPerson(mockMvc, "91" + (System.nanoTime() % 1_000_000));
        return createBudget(mockMvc, clientId, propertyAmount);
    }

    public static BigDecimal pendingBalance(MockMvc mockMvc, int budgetId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/pagos/presupuesto/" + budgetId + "/saldo"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode root = MAPPER.readTree(result.getResponse().getContentAsString());
        if (root.isNumber()) {
            return root.decimalValue();
        }
        if (root.has("pendingBalance")) {
            return root.get("pendingBalance").decimalValue();
        }
        if (root.has("saldoPendiente")) {
            return root.get("saldoPendiente").decimalValue();
        }
        throw new IllegalStateException("Unexpected saldo payload: " + root);
    }

    public static void createPayment(MockMvc mockMvc, int budgetId, BigDecimal amount, String notes)
            throws Exception {
        String body = """
                {"idBudget": %d, "amount": %s, "date": "2026-01-15", "notes": "%s"}
                """.formatted(budgetId, amount.toPlainString(), notes);
        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
