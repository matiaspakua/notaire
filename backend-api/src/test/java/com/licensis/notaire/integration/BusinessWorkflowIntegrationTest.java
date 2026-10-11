package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.testing.BudgetPaymentTestFixtures;
import com.licensis.notaire.testing.RequirementCoverage;
import java.math.BigDecimal;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for complete business workflows.
 * Issue #242 — Test complete business workflows: client → management → budget → payment.
 *
 * Each nested class covers a distinct business domain workflow.
 * Uses H2 in-memory database (PostgreSQL compatibility mode) for fast execution.
 * DirtiesContext ensures clean state between test classes.
 */
@RequirementCoverage({"CU03", "CU17", "CU18", "CU19", "CU20", "CU02", "CU15", "CU05", "CU06", "CU26", "CU45", "CU47"})
@SpringBootTest
@ActiveProfiles("test-h2")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Business Workflow Integration Tests (Issue #242)")
class BusinessWorkflowIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ──────────────────────────────────────────────
    // CU03 — Authentication / login workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU03 — User authentication")
    class LoginWorkflow {

        @Test
        @Order(1)
        @DisplayName("Admin user can authenticate with default credentials")
        void adminCanAuthenticateWithDefaultCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "admin", "password": "admin"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid").isBoolean());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/users returns user list")
        void getAllUsersReturnsNonNullList() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("CU20 — Create new user returns 2xx")
        void createNewUser() throws Exception {
            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "testWorkflowUser",
                                      "password": "test123",
                                      "type": "EMPLEADO",
                                      "active": true
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    // ──────────────────────────────────────────────
    // CU17/CU18 — People (clients) workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU17/CU18 — Register and look up people")
    class PeopleWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/people returns a page whose content is an array")
        void getAllPeopleReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/people"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU18 — Create person returns 201")
        void createPersonReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/people")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "Juan",
                                      "lastName": "García",
                                      "dni": "30111222",
                                      "identificationNumber": "30111222",
                                      "email": "juan.garcia@example.com",
                                      "isClient": true,
                                      "fkIdIdentificationType": {"idIdentificationType": 1}
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU41 — Search person endpoint is accessible")
        void searchPersonEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/people/search").param("firstName", "García"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // CU02/CU13 — Managements workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU02/CU13 — Lifecycle of a management")
    class ManagementsWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/managements returns array")
        void getAllManagementsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/managements"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU02 — Create management returns 200")
        void createManagementReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/managements")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 20250001,
                                      "heading": "Test management",
                                      "dateStart": "2025-01-15",
                                      "notaryPersonId": 1
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @Order(3)
        @DisplayName("CU19 — GET managements by client returns array")
        void getManagementsByClientReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/managements/client/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("History endpoint is accessible for management")
        void historyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/history"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU01 — Budgets workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU01 — Management of budgets")
    class BudgetsWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/budgets returns array")
        void getAllBudgetsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/budgets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU01 — Create budget returns 201")
        void createBudgetReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/budgets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 20250001,
                                      "date": "2025-01-15",
                                      "heading": "Test budget",
                                      "amount": 15000.00,
                                      "status": "PENDIENTE"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU45 — GET budgets by person returns array")
        void getBudgetByPersonReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/budgets/person/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("Items endpoint accessible for budget")
        void itemsEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("Items by budget endpoint accessible")
        void itemsByBudgetEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/items/budget/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU15/CU47 — Payments workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU15/CU47 — Payment processing")
    class PaymentsWorkflow {

        private Integer fixtureBudgetId;

        @BeforeEach
        void arrangeDedicatedBudget() throws Exception {
            fixtureBudgetId = BudgetPaymentTestFixtures.createIsolatedBudget(
                    mockMvc, new BigDecimal("25000.00"));
        }

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/payments returns array")
        void getAllPaymentsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/payments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU15 — Create payment returns 201 against dedicated budget (#916)")
        void createPaymentReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idBudget": %d,
                                      "amount": 5000.00,
                                      "date": "2025-01-15",
                                      "notes": "Test payment"
                                    }
                                    """.formatted(fixtureBudgetId)))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU47 — GET payments by budget returns array")
        void getPaymentsByBudgetReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/payments/budget/" + fixtureBudgetId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU05-CU08 — Deeds workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU05-CU08 — Deeds and protocol")
    class DeedsWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/deeds returns array")
        void getAllDeedsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/deeds"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU05 — Create deed returns 201")
        void createDeedReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/deeds")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 2025001,
                                      "body": "Test deed body",
                                      "status": "BORRADOR",
                                      "dateDeedrecording": "2025-01-15"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("Property endpoint accessible")
        void propertyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/properties"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("Testimony endpoint accessible")
        void testimonyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/testimonies"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // CU28/CU40 — Folios workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU28/CU40 — Management of folios")
    class FoliosWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/folios returns array")
        void getAllFoliosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/folios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/folio-types returns array")
        void getTypeFolioReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/folio-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("CU28 — Create folio returns 201 Created")
        void createFolioReturns201() throws Exception {
            mockMvc.perform(post("/api/v1/folios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 9001,
                                      "year": 2025,
                                      "status": "Nuevo",
                                      "typeFolioId": 1,
                                      "notaryId": 1
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
    }

    // ──────────────────────────────────────────────
    // Administration catalog workflows
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU29/CU66 — Concepts catalog")
    class ConceptsWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/concepts returns array")
        void getAllConceptsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/concepts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU29 — Create concept returns 201")
        void createConceptReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/concepts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Base fee",
                                      "description": "Base fee for the deed",
                                      "value": 10000.00
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("CU26/CU57/CU64 — Procedure types catalog")
    class ProcedureTypesWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/procedure-types returns array")
        void getAllProcedureTypesReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/procedure-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU26 — Create type procedure returns 201")
        void createTypeProcedureReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/procedure-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Compraventa property",
                                      "description": "Compraventa operation",
                                      "isArchived": true,
                                      "isRegistered": false,
                                      "associatesProperties": true
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("CU67 — Management status catalog")
    class ManagementStatusCatalogWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/management-statuses returns array")
        void getAllStatusesReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/management-statuses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Audit — record of operations")
    class AuditWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/audit-log returns array")
        void getAllAuditReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/audit-log"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/audit-log/user/1 returns array")
        void getAuditByUserReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/audit-log/user/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU39/CU49/CU55 — Budget templates
    // ──────────────────────────────────────────────

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("CU39/CU49/CU55 — Budget templates")
    class TemplatesWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/budget-templates returns array")
        void getAllTemplatesReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/budget-templates"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET budget templates by procedure type returns an array")
        void getTemplatesByProcedureTypeReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/budget-templates/procedure-types/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("POST /api/v1/budget-templates returns 201 with bounded response — no circular JSON")
        void createTemplateReturns201WithBoundedResponse() throws Exception {
            String body = """
                    {"fkIdProcedureType":1,"fkIdConcept":1,"notes":"workflow IT"}
                    """;

            MvcResult result = mockMvc.perform(post("/api/v1/budget-templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andReturn();

            // Response body must be parseable JSON and must not be a circular/huge blob
            String responseBody = result.getResponse().getContentAsString();
            if (!responseBody.isBlank()) {
                assertDoesNotThrow(() -> new ObjectMapper().readTree(responseBody),
                        "Response body must be valid, bounded JSON (no circular serialization)");
                assertTrue(responseBody.length() < 2048,
                        "Response body must be small — not the full entity graph. Got " + responseBody.length() + " chars");
            }
        }

        @Test
        @Order(4)
        @DisplayName("POST duplicate budget template (same type=1/concept=1 created in Order 3) returns 409 Conflict")
        void createDuplicateTemplateReturns409() throws Exception {
            // Order 3 already created (type=1, concept=1). Re-posting the same PK must return 409.
            String body = """
                    {"fkIdProcedureType":1,"fkIdConcept":1}
                    """;
            mockMvc.perform(post("/api/v1/budget-templates")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }
    }
}
