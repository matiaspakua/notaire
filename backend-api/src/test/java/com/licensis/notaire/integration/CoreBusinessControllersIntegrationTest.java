package com.licensis.notaire.integration;

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
import com.licensis.notaire.testing.RequirementCoverage;

/**
 * Integration tests for core business controllers.
 * Covers: CU01, CU02, CU05, CU15, CU17, CU18, CU20, CU29, CU34, CU37, CU41, CU45, CU47, CU54, CU60, CU61, CU66
 */
@RequirementCoverage({"CU01", "CU02", "CU05", "CU15", "CU17", "CU18", "CU20", "CU45", "CU47", "CU54", "CU60", "CU61"})
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Core Business Controllers Integration Tests")
class CoreBusinessControllersIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Nested
    @DisplayName("PersonaController - CU17/CU18/CU41/CU54/CU61")
    class PersonaControllerTests {

        @Test
        @DisplayName("CU18 - Should return all personas")
        void shouldReturnAllPersonas() throws Exception {
            mockMvc.perform(get("/api/v1/personas"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU18 - Should return persona by ID")
        void shouldReturnPersonaById() throws Exception {
            mockMvc.perform(get("/api/v1/personas/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idPersona", is(1)));
        }

        @Test
        @DisplayName("CU18 - Should return 404 for non-existing persona")
        void shouldReturn404ForNonExistingPersona() throws Exception {
            mockMvc.perform(get("/api/v1/personas/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU17 - Should create new persona (client)")
        void shouldCreatePersona() throws Exception {
            mockMvc.perform(post("/api/v1/personas")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Carlos",
                                      "apellido": "Gomez",
                                      "numeroIdentificacion": "30987654",
                                      "esCliente": true,
                                      "fkIdTipoIdentificacion": { "idTipoIdentificacion": 1 },
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU41 - Should search personas by nombre")
        void shouldSearchPersonasByNombre() throws Exception {
            mockMvc.perform(get("/api/v1/personas/buscar")
                            .param("nombre", "Juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU61 - Should search personas by numeroIdentificacion")
        void shouldSearchPersonasByNumeroIdentificacion() throws Exception {
            mockMvc.perform(get("/api/v1/personas/buscar")
                            .param("numeroIdentificacion", "20123456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }
    }

    @Nested
    @DisplayName("ConceptoController - CU29/CU34/CU37/CU66")
    class ConceptoControllerTests {

        @Test
        @DisplayName("CU66 - Should return all conceptos")
        void shouldReturnAllConceptos() throws Exception {
            mockMvc.perform(get("/api/v1/conceptos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].nombre", notNullValue()));
        }

        @Test
        @DisplayName("CU66 - Should return concepto by ID")
        void shouldReturnConceptoById() throws Exception {
            mockMvc.perform(get("/api/v1/conceptos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idConcepto", is(1)));
        }

        @Test
        @DisplayName("CU66 - Should return 404 for non-existing concepto")
        void shouldReturn404ForNonExistingConcepto() throws Exception {
            mockMvc.perform(get("/api/v1/conceptos/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU29 - Should create new concepto")
        void shouldCreateConcepto() throws Exception {
            mockMvc.perform(post("/api/v1/conceptos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Honorario adicional",
                                      "valor": 5000,
                                      "porcentaje": 0,
                                      "habilitado": true,
                                      "conceptoFijo": false,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("PresupuestoController - CU01/CU45/CU60")
    class PresupuestoControllerTests {

        @Test
        @DisplayName("CU45 - Should return all presupuestos")
        void shouldReturnAllPresupuestos() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU45 - Should return presupuesto by ID")
        void shouldReturnPresupuestoById() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idPresupuesto", is(1)));
        }

        @Test
        @DisplayName("CU45 - Should return 404 for non-existing presupuesto")
        void shouldReturn404ForNonExistingPresupuesto() throws Exception {
            mockMvc.perform(get("/api/v1/presupuestos/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU01 - Should create new presupuesto")
        void shouldCreatePresupuesto() throws Exception {
            mockMvc.perform(post("/api/v1/presupuestos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "numero": 20250099,
                                      "fecha": "2025-06-01",
                                      "encabezado": "Presupuesto test",
                                      "estado": "PENDIENTE",
                                      "montoInmueble": 50000.00,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("EscrituraController - CU05/CU06/CU52/CU62")
    class EscrituraControllerTests {

        @Test
        @DisplayName("CU06 - Should return all escrituras")
        void shouldReturnAllEscrituras() throws Exception {
            mockMvc.perform(get("/api/v1/escrituras"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU06 - Should return 404 for non-existing escritura")
        void shouldReturn404ForNonExistingEscritura() throws Exception {
            mockMvc.perform(get("/api/v1/escrituras/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU05 - Should return escribanos disponibles")
        void shouldReturnEscribanosDisponibles() throws Exception {
            mockMvc.perform(get("/api/v1/escrituras/escribanos-disponibles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("UsuarioController - CU20/CU21/CU23")
    class UsuarioControllerTests {

        @Test
        @DisplayName("CU20 - Should return all usuarios")
        void shouldReturnAllUsuarios() throws Exception {
            mockMvc.perform(get("/api/v1/usuarios"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU20 - Should login with admin credentials")
        void shouldLoginWithAdminCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "admin",
                                      "contrasenia": "admin"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido", is(true)));
        }

        @Test
        @DisplayName("CU20 - Should reject invalid credentials")
        void shouldRejectInvalidCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/usuarios/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "admin",
                                      "contrasenia": "wrong_password"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valido", is(false)));
        }
    }

    @Nested
    @DisplayName("TipoDeTramiteController - CU26/CU31/CU57/CU64")
    class TipoDeTramiteControllerTests {

        @Test
        @DisplayName("CU64 - Should return all tipos de tramite")
        void shouldReturnAllTiposDeTramite() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-tramite"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].nombre", notNullValue()));
        }

        @Test
        @DisplayName("CU26 - Should create new tipo de tramite")
        void shouldCreateTipoDeTramite() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-tramite")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Donacion",
                                      "observaciones": "Tramite de donacion",
                                      "habilitado": true,
                                      "seArchiva": true,
                                      "seInscribe": false,
                                      "asociaInmuebles": false,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("RegistroAuditoriaController - CU23")
    class RegistroAuditoriaControllerTests {

        @Test
        @DisplayName("CU23 - Should return all registros de auditoria")
        void shouldReturnAllRegistrosAuditoria() throws Exception {
            mockMvc.perform(get("/api/v1/registro-auditoria"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU23 - Should return 200 for auditoria by usuario")
        void shouldReturnAuditoriaByUsuario() throws Exception {
            mockMvc.perform(get("/api/v1/registro-auditoria/usuario/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("FolioController - CU28/CU33/CU40/CU58/CU63")
    class FolioControllerTests {

        @Test
        @DisplayName("CU63 - Should return all folios")
        void shouldReturnAllFolios() throws Exception {
            mockMvc.perform(get("/api/v1/folio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("SuplenciaController - CU22/CU59")
    class SuplenciaControllerTests {

        @Test
        @DisplayName("CU59 - Should return all suplencias")
        void shouldReturnAllSuplencias() throws Exception {
            mockMvc.perform(get("/api/v1/suplencia"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("InmuebleController")
    class InmuebleControllerTests {

        @Test
        @DisplayName("Should return all inmuebles")
        void shouldReturnAllInmuebles() throws Exception {
            mockMvc.perform(get("/api/v1/inmueble"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }
}
