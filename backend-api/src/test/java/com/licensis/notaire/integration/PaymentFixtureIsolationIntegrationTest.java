package com.licensis.notaire.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.licensis.notaire.testing.BudgetPaymentTestFixtures;
import com.licensis.notaire.testing.RequirementCoverage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Issue #916 — proves payment mutation ITs must not hardcode seed {@code idBudget=1}.
 * After the seed presupuesto is exhausted, a dedicated fixture still accepts payments
 * while a further payment against seed id 1 is rejected by the #848 guard.
 */
@RequirementCoverage({"CU15", "CU76"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Payment fixture isolation — seed budget vs dedicated presupuesto (#916)")
class PaymentFixtureIsolationIntegrationTest {

    private static final int SEED_BUDGET_ID = 1;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Should accept payment against dedicated budget after seed budget is exhausted")
    void shouldAcceptPaymentAgainstDedicatedBudgetAfterSeedBudgetExhausted() throws Exception {
        exhaustSeedBudgetIfNeeded();

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idBudget": 1,
                                  "amount": 100.00,
                                  "date": "2026-01-15",
                                  "notes": "Hardcoded seed payment after exhaustion (#916)"
                                }
                                """))
                .andExpect(status().isConflict());

        Integer dedicatedBudgetId = BudgetPaymentTestFixtures.createIsolatedBudget(
                mockMvc, new BigDecimal("5000.00"));
        BudgetPaymentTestFixtures.createPayment(
                mockMvc,
                dedicatedBudgetId,
                new BigDecimal("100.00"),
                "Dedicated fixture payment after seed exhaustion (#916)");
    }

    private void exhaustSeedBudgetIfNeeded() throws Exception {
        BigDecimal remaining = BudgetPaymentTestFixtures.pendingBalance(mockMvc, SEED_BUDGET_ID);
        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            BudgetPaymentTestFixtures.createPayment(
                    mockMvc, SEED_BUDGET_ID, remaining, "Exhaust seed budget for #916 isolation proof");
        }
    }
}
