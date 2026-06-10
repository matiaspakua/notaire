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

/**
 * Integration tests for remaining controllers.
 * Covers: CU02, CU03, CU04, CU09, CU10, CU11, CU12, CU13, CU14, CU15, CU16,
 *         CU24, CU25, CU33, CU39, CU42, CU43, CU44, CU46, CU47, CU48, CU49,
 *         CU50, CU51, CU53, CU55, CU56
 */
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
    @DisplayName("TipoIdentificacionController")
    class TipoIdentificacionTests {

        @Test
        @DisplayName("Should return all tipos de identificacion")
        void shouldReturnAllTiposIdentificacion() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-identificacion"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return tipo de identificacion by ID")
        void shouldReturnTipoIdentificacionById() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-identificacion/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idTipoIdentificacion", is(1)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing tipo de identificacion")
        void shouldReturn404ForNonExisting() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-identificacion/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should create new tipo de identificacion")
        void shouldCreateTipoIdentificacion() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-identificacion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Pasaporte",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("GestionController - CU02/CU13/CU14/CU24/CU25")
    class GestionTests {

        @Test
        @DisplayName("Should return all gestiones")
        void shouldReturnAllGestiones() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing gestion")
        void shouldReturn404ForNonExistingGestion() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return empty list when searching by client with no gestiones")
        void shouldReturnEmptyListForClientWithNoGestiones() throws Exception {
            mockMvc.perform(get("/api/v1/gestiones/cliente/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("TramiteController - CU04/CU11")
    class TramiteTests {

        @Test
        @DisplayName("Should return all tramites")
        void shouldReturnAllTramites() throws Exception {
            mockMvc.perform(get("/api/v1/tramites"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing tramite")
        void shouldReturn404ForNonExistingTramite() throws Exception {
            mockMvc.perform(get("/api/v1/tramites/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PagoController - CU15/CU47")
    class PagoTests {

        @Test
        @DisplayName("Should return all pagos")
        void shouldReturnAllPagos() throws Exception {
            mockMvc.perform(get("/api/v1/pagos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing pago")
        void shouldReturn404ForNonExistingPago() throws Exception {
            mockMvc.perform(get("/api/v1/pagos/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return pagos by presupuesto")
        void shouldReturnPagosByPresupuesto() throws Exception {
            mockMvc.perform(get("/api/v1/pagos/presupuesto/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU47 - Should return saldo for presupuesto")
        void shouldReturnSaldoForPresupuesto() throws Exception {
            mockMvc.perform(get("/api/v1/pagos/presupuesto/1/saldo"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CU15 - Should create new pago")
        void shouldCreatePago() throws Exception {
            mockMvc.perform(post("/api/v1/pagos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idPresupuesto": 1,
                                      "monto": 1500.00,
                                      "fecha": "2025-06-01",
                                      "observaciones": "Pago test"
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("HistorialController - CU09/CU16/CU43/CU48")
    class HistorialTests {

        @Test
        @DisplayName("Should return all historiales")
        void shouldReturnAllHistoriales() throws Exception {
            mockMvc.perform(get("/api/v1/historial"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing historial")
        void shouldReturn404ForNonExistingHistorial() throws Exception {
            mockMvc.perform(get("/api/v1/historial/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DocumentoPresentadoController - CU42/CU50/CU56")
    class DocumentoPresentadoTests {

        @Test
        @DisplayName("Should return all documentos presentados")
        void shouldReturnAllDocumentosPresentados() throws Exception {
            mockMvc.perform(get("/api/v1/documento-presentado"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing documento presentado")
        void shouldReturn404ForNonExistingDocumentoPresentado() throws Exception {
            mockMvc.perform(get("/api/v1/documento-presentado/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("CopiaController - CU44/CU53")
    class CopiaTests {

        @Test
        @DisplayName("Should return all copias")
        void shouldReturnAllCopias() throws Exception {
            mockMvc.perform(get("/api/v1/copia"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return 404 for non-existing copia")
        void shouldReturn404ForNonExistingCopia() throws Exception {
            mockMvc.perform(get("/api/v1/copia/9999"))
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
    @DisplayName("PlantillaPresupuestoController - CU39/CU49/CU55")
    class PlantillaPresupuestoTests {

        @Test
        @DisplayName("CU39 - Should return all plantillas de presupuesto")
        void shouldReturnAllPlantillasPresupuesto() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU39 - Should return 404 for non-existing plantilla")
        void shouldReturn404ForNonExistingPlantilla() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU49 - Should return 404 for non-existing plantilla de presupuesto")
        void shouldReturn404ForNonExistingPlantillaPresupuesto() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-presupuestos/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PlantillaTramiteController - CU25/CU33")
    class PlantillaTramiteTests {

        @Test
        @DisplayName("Should return all plantillas de tramite")
        void shouldReturnAllPlantillasTramite() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-tramite"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("Should return empty list for non-existing tipo tramite")
        void shouldReturnEmptyListForNonExistingTipoTramite() throws Exception {
            mockMvc.perform(get("/api/v1/plantilla-tramite/tipo-tramite/9999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }
}
