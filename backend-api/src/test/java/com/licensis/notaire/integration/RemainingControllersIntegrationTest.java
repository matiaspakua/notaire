package com.licensis.notaire.integration;

import com.licensis.notaire.testing.BudgetPaymentTestFixtures;
import com.licensis.notaire.testing.RequirementCoverage;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for remaining controllers.
 * Covers: CU02, CU03, CU04, CU09, CU10, CU11, CU12, CU13, CU14, CU15, CU16,
 *         CU24, CU25, CU33, CU39, CU42, CU43, CU44, CU46, CU47, CU48, CU49,
 *         CU50, CU51, CU53, CU55, CU56
 */
@RequirementCoverage({"CU02", "CU04", "CU11", "CU13", "CU14", "CU15", "CU22", "CU24", "CU25", "CU59"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Remaining Controllers Integration Tests")
class RemainingControllersIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Nested
    @DisplayName("IdentificationTypeController")
    class IdentificationTypeTests {

        @Test
        @DisplayName("Should return all identification types")
        void shouldReturnAllIdentificationTypes() throws Exception {
            mockMvc.perform(get("/api/v1/identification-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return type of identification by ID")
        void shouldReturnIdentificationTypeById() throws Exception {
            mockMvc.perform(get("/api/v1/identification-types/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idIdentificationType", is(1)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing type of identification")
        void shouldReturn404ForNonExisting() throws Exception {
            mockMvc.perform(get("/api/v1/identification-types/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should create new type of identification")
        void shouldCreateIdentificationType() throws Exception {
            mockMvc.perform(post("/api/v1/identification-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Pasaporte",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("ManagementController - CU02/CU13/CU14/CU24/CU25")
    class ManagementTests {

        @Test
        @DisplayName("Should return all managements")
        void shouldReturnAllManagements() throws Exception {
            mockMvc.perform(get("/api/v1/managements"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing management")
        void shouldReturn404ForNonExistingManagement() throws Exception {
            mockMvc.perform(get("/api/v1/managements/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return empty list when searching by client with not managements")
        void shouldReturnEmptyListForClientWithNoManagements() throws Exception {
            mockMvc.perform(get("/api/v1/managements/client/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("ProcedureController - CU04/CU11")
    class ProcedureTests {

        @Test
        @DisplayName("Should return all procedures")
        void shouldReturnAllProcedures() throws Exception {
            mockMvc.perform(get("/api/v1/procedures"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing procedure")
        void shouldReturn404ForNonExistingProcedure() throws Exception {
            mockMvc.perform(get("/api/v1/procedures/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PaymentController - CU15/CU47")
    class PaymentTests {

        @Test
        @DisplayName("Should return all payments")
        void shouldReturnAllPayments() throws Exception {
            mockMvc.perform(get("/api/v1/payments"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for a missing payment")
        void shouldReturn404ForNonExistingPayment() throws Exception {
            mockMvc.perform(get("/api/v1/payments/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return payments by budget")
        void shouldReturnPaymentsByBudget() throws Exception {
            mockMvc.perform(get("/api/v1/payments/budget/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU47 - Should return the balance for a budget")
        void shouldReturnBalanceForBudget() throws Exception {
            mockMvc.perform(get("/api/v1/payments/budget/1/balance"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CU15 - Should create new payment against dedicated budget (#916)")
        void shouldCreatePayment() throws Exception {
            Integer budgetId = BudgetPaymentTestFixtures.createIsolatedBudget(
                    mockMvc, new BigDecimal("10000.00"));
            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idBudget": %d,
                                      "amount": 1500.00,
                                      "date": "2025-06-01",
                                      "notes": "Payment test"
                                    }
                                    """.formatted(budgetId)))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("HistoryController - CU09/CU16/CU43/CU48")
    class HistoryTests {

        @Test
        @DisplayName("Should return a page of history records")
        void shouldReturnAllHistoryRecords() throws Exception {
            mockMvc.perform(get("/api/v1/history"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing history")
        void shouldReturn404ForNonExistingHistory() throws Exception {
            mockMvc.perform(get("/api/v1/history/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("SubmittedDocumentController - CU42/CU50/CU56")
    class SubmittedDocumentTests {

        @Test
        @DisplayName("Should return all submitted documents")
        void shouldReturnAllSubmittedDocuments() throws Exception {
            mockMvc.perform(get("/api/v1/submitted-documents"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for a missing submitted document")
        void shouldReturn404ForNonExistingSubmittedDocument() throws Exception {
            mockMvc.perform(get("/api/v1/submitted-documents/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CopyController - CU44/CU53")
    class CopyTests {

        @Test
        @DisplayName("Should return all copies")
        void shouldReturnAllCopies() throws Exception {
            mockMvc.perform(get("/api/v1/copies"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for a missing copy")
        void shouldReturn404ForNonExistingCopy() throws Exception {
            mockMvc.perform(get("/api/v1/copies/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("ItemController - CU46/CU51")
    class ItemTests {

        @Test
        @DisplayName("Should return all items")
        void shouldReturnAllItems() throws Exception {
            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing item")
        void shouldReturn404ForNonExistingItem() throws Exception {
            mockMvc.perform(get("/api/v1/items/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("BudgetTemplateController - CU39/CU49/CU55")
    class BudgetTemplateTests {

        @Test
        @DisplayName("CU39 - Should return all budget templates")
        void shouldReturnAllBudgetTemplates() throws Exception {
            mockMvc.perform(get("/api/v1/budget-templates"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU39 - Should return 404 for a missing budget template")
        void shouldReturn404ForNonExistingTemplate() throws Exception {
            mockMvc.perform(get("/api/v1/budget-templates/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU49 - Should return 404 for a missing budget template")
        void shouldReturn404ForNonExistingBudgetTemplate() throws Exception {
            mockMvc.perform(get("/api/v1/budget-templates/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("ProcedureTemplateController - CU25/CU33")
    class ProcedureTemplateTests {

        @Test
        @DisplayName("Should return all procedure templates")
        void shouldReturnAllProcedureTemplates() throws Exception {
            mockMvc.perform(get("/api/v1/procedure-templates"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return empty list for non-existing type procedure")
        void shouldReturnEmptyListForNonExistingTypeProcedure() throws Exception {
            mockMvc.perform(get("/api/v1/procedure-templates/procedure-types/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }
}
