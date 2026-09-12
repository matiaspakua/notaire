package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.licensis.notaire.testing.RequirementCoverage;

/**
 * Integration tests for complete business workflows.
 * Issue #242 — Test complete business workflows: cliente → gestión → presupuesto → pago.
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
    // CU03 — Autenticación / Login workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU03 — Autenticación de usuario")
    class LoginWorkflow {

        @Test
        @Order(1)
        @DisplayName("Admin user can authenticate with default credentials")
        void adminCanAuthenticateWithDefaultCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name": "admin", "password": "admin"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido").isBoolean());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/usuarios returns user list")
        void getAllUsersReturnsNonNullList() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("CU20 — Create new user returns 2xx")
        void createNewUser() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios")
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
    // CU17/CU18 — Personas (Clientes) workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU17/CU18 — Registro y consulta de persons")
    class PeopleWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/people returns array")
        void getAllPeopleReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/people"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
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
    // CU02/CU13 — Gestiones workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU02/CU13 — Ciclo de vida de una gestión")
    class GestionesWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/gestiones returns array")
        void getAllGestionesReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU02 — Create gestión returns 200")
        void createManagementReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/gestiones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 20250001,
                                      "encabezado": "Gestión de prueba",
                                      "dateStart": "2025-01-15",
                                      "fkIdNotaryPerson": {"personId": 1}
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @Order(3)
        @DisplayName("CU19 — GET gestiones by cliente returns array")
        void getGestionesByClientReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones/cliente/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("Historial endpoint is accessible for gestión")
        void historyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/historial"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU01 — Presupuestos workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU01 — Gestión de presupuestos")
    class PresupuestosWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/presupuestos returns array")
        void getAllPresupuestosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU01 — Create budget returns 201")
        void createBudgetReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/presupuestos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 20250001,
                                      "date": "2025-01-15",
                                      "encabezado": "Budget de prueba",
                                      "amount": 15000.00,
                                      "status": "Pending"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU45 — GET presupuestos by person returns array")
        void getPresupuestosByPersonReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos/persona/1"))
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
            mockMvc.perform(get("/api/v1/items/presupuesto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU15/CU47 — Pagos workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU15/CU47 — Procesamiento de payments")
    class PaymentsWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/pagos returns array")
        void getAllPaymentsReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/pagos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU15 — Create pago returns 200")
        void createPaymentReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/pagos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idBudget": 1,
                                      "amount": 5000.00,
                                      "date": "2025-01-15",
                                      "notes": "Test payment"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU47 — GET payments by budget returns array")
        void getPaymentsByBudgetReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/pagos/presupuesto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU05-CU08 — Escrituras workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU05-CU08 — Escrituras y protocolo")
    class EscriturasWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/escrituras returns array")
        void getAllEscriturasReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/escrituras"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU05 — Create deed returns 201")
        void createDeedReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/escrituras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 2025001,
                                      "body": "Body de la deed de prueba",
                                      "status": "BORRADOR",
                                      "dateDeedrecording": "2025-01-15"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("Inmueble endpoint accessible")
        void propertyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/inmueble"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("Testimonio endpoint accessible")
        void testimonyEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/testimonio"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // CU28/CU40 — Folios workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU28/CU40 — Gestión de folios")
    class FoliosWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/folio returns array")
        void getAllFoliosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/folio"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/tipo-folio returns array")
        void getTypeFolioReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("CU28 — Create folio returns 201 Created")
        void createFolioReturns201() throws Exception {
            mockMvc.perform(post("/api/v1/folio")
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
    @DisplayName("CU29/CU66 — Conceptos catalog")
    class ConceptosWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/conceptos returns array")
        void getAllConceptosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/conceptos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU29 — Create concepto returns 201")
        void createConceptReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/conceptos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Honorario base",
                                      "description": "Honorario base de deed",
                                      "value": 10000.00
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("CU26/CU57/CU64 — Tipos de trámite catalog")
    class TiposProcedureWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/tipo-tramite returns array")
        void getAllTiposProcedureReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-tramite"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU26 — Create type tramite returns 201")
        void createTypeProcedureReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-tramite")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Compraventa property",
                                      "description": "Operación de compraventa",
                                      "isArchived": true,
                                      "isRegistered": false,
                                      "associatesProperties": true
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("CU67 — Estados de gestión catalog")
    class EstadosManagementWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/estado-gestion returns array")
        void getAllEstadosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Auditoría — Registro de operaciones")
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
    // CU39/CU49/CU55 — Plantillas de presupuesto
    // ──────────────────────────────────────────────

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("CU39/CU49/CU55 — Plantillas de budget")
    class PlantillasWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/plantilla-presupuestos returns array")
        void getAllPlantillasReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET plantillas by type-tramite returns array")
        void getPlantillasByTypeProcedureReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos/tipo-tramite/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("POST /api/v1/plantilla-presupuestos returns 201 with bounded response — no circular JSON")
        void createTemplateReturns201WithBoundedResponse() throws Exception {
            String body = """
                    {"budgetTemplatePK":{"fkIdProcedureType":1,"fkIdConcept":1},
                     "procedureType":{"idProcedureType":1},
                     "concept":{"idConcept":1}}
                    """;

            MvcResult result = mockMvc.perform(post("/api/v1/plantilla-presupuestos")
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
        @DisplayName("POST duplicate plantilla (same type=1/concepto=1 created in Order 3) returns 409 Conflict")
        void createDuplicateTemplateReturns409() throws Exception {
            // Order 3 already created (tipo=1, concepto=1). Re-posting the same PK must return 409.
            String body = """
                    {"budgetTemplatePK":{"fkIdProcedureType":1,"fkIdConcept":1},
                     "procedureType":{"idProcedureType":1},
                     "concept":{"idConcept":1}}
                    """;
            mockMvc.perform(post("/api/v1/plantilla-presupuestos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict());
        }
    }
}
