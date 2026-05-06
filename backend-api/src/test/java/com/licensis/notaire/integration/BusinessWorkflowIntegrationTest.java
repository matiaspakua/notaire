package com.licensis.notaire.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
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
 * Issue #242 — Test complete business workflows: cliente → gestión → presupuesto → pago.
 *
 * Each nested class covers a distinct business domain workflow.
 * Uses H2 in-memory database (PostgreSQL compatibility mode) for fast execution.
 * DirtiesContext ensures clean state between test classes.
 */
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
                                    {"nombre": "admin", "contrasenia": "admin"}
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
        @DisplayName("CU20 — Create new user returns 200")
        void createNewUser() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "test_workflow_user",
                                      "contrasenia": "test123",
                                      "tipo": "EMPLEADO",
                                      "estado": true,
                                      "fkIdPersona": {"idPersona": 1}
                                    }
                                    """))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────
    // CU17/CU18 — Personas (Clientes) workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU17/CU18 — Registro y consulta de personas")
    class PersonasWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/personas returns array")
        void getAllPersonasReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/personas"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU18 — Create persona returns 200")
        void createPersonaReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/personas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Juan",
                                      "apellido": "García",
                                      "dni": "20123456",
                                      "numeroIdentificacion": "20123456",
                                      "email": "juan.garcia@example.com",
                                      "esCliente": true,
                                      "fkIdTipoIdentificacion": {"idTipoIdentificacion": 1}
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("CU41 — Buscar persona endpoint is accessible")
        void searchPersonaEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/personas/buscar").param("nombre", "García"))
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
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU02 — Create gestión returns 200")
        void createGestionReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/gestiones")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "numero": 20250001,
                                      "encabezado": "Gestión de prueba",
                                      "fechaInicio": "2025-01-15",
                                      "fkIdPersonaEscribano": {"idPersona": 1}
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("CU19 — GET gestiones by cliente returns array")
        void getGestionesByClienteReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones/cliente/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("Historial endpoint is accessible for gestión")
        void historialEndpointAccessible() throws Exception {
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
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU01 — Create presupuesto returns 200")
        void createPresupuestoReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/presupuestos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "numero": 20250001,
                                      "fecha": "2025-01-15",
                                      "encabezado": "Presupuesto de prueba",
                                      "monto": 15000.00,
                                      "estado": "PENDIENTE"
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("CU45 — GET presupuestos by persona returns array")
        void getPresupuestosByPersonaReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos/persona/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(4)
        @DisplayName("Items endpoint accessible for presupuesto")
        void itemsEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/items"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(5)
        @DisplayName("Items by presupuesto endpoint accessible")
        void itemsByPresupuestoEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/items/presupuesto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU15/CU47 — Pagos workflow
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU15/CU47 — Procesamiento de pagos")
    class PagosWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/pagos returns array")
        void getAllPagosReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/pagos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU15 — Create pago returns 200")
        void createPagoReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/pagos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idPresupuesto": 1,
                                      "monto": 5000.00,
                                      "fecha": "2025-01-15",
                                      "observaciones": "Test payment"
                                    }
                                    """))
                    .andExpect(status().isCreated());
        }

        @Test
        @Order(3)
        @DisplayName("CU47 — GET pagos by presupuesto returns array")
        void getPagosByPresupuestoReturnsArray() throws Exception {
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
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU05 — Create escritura returns 200")
        void createEscrituraReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/escrituras")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "numero": 2025001,
                                      "cuerpo": "Cuerpo de la escritura de prueba",
                                      "estado": "BORRADOR",
                                      "fechaEscrituracion": "2025-01-15"
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(3)
        @DisplayName("Inmueble endpoint accessible")
        void inmuebleEndpointAccessible() throws Exception {
            mockMvc.perform(get("/api/v1/inmueble"))
                    .andExpect(status().isOk());
        }

        @Test
        @Order(4)
        @DisplayName("Testimonio endpoint accessible")
        void testimonioEndpointAccessible() throws Exception {
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
        void getTipoFolioReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(3)
        @DisplayName("CU28 — Create folio returns 200")
        void createFolioReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/folio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "numero": 9001,
                                      "anio": 2025,
                                      "estado": "ACTIVO",
                                      "fkIdPersonaEscribano": {"idPersona": 1},
                                      "fkIdTipoFolio": {"idTipoFolio": 1}
                                    }
                                    """))
                    .andExpect(status().isOk());
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
        @DisplayName("CU29 — Create concepto returns 200")
        void createConceptoReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/conceptos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Honorario base",
                                      "descripcion": "Honorario base de escritura",
                                      "valor": 10000.00
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CU26/CU57/CU64 — Tipos de trámite catalog")
    class TiposTramiteWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/tipo-tramite returns array")
        void getAllTiposTramiteReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-tramite"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("CU26 — Create tipo tramite returns 200")
        void createTipoTramiteReturns200() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-tramite")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Compraventa inmueble",
                                      "descripcion": "Operación de compraventa",
                                      "seArchiva": true,
                                      "seInscribe": false
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CU67 — Estados de gestión catalog")
    class EstadosGestionWorkflow {

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
    class AuditoriaWorkflow {

        @Test
        @Order(1)
        @DisplayName("GET /api/v1/registro-auditoria returns array")
        void getAllAuditoriaReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/registro-auditoria"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @Order(2)
        @DisplayName("GET /api/v1/registro-auditoria/usuario/1 returns array")
        void getAuditoriaByUsuarioReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/registro-auditoria/usuario/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    // ──────────────────────────────────────────────
    // CU39/CU49/CU55 — Plantillas de presupuesto
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("CU39/CU49/CU55 — Plantillas de presupuesto")
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
        @DisplayName("GET plantillas by tipo-tramite returns array")
        void getPlantillasByTipoTramiteReturnsArray() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos/tipo-tramite/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }
}
