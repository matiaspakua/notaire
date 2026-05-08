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
 * Integration tests for catalog controllers that were previously returning 500 errors.
 * Covers: CU27, CU30, CU32, CU35, CU36, CU38, CU40, CU58, CU65, CU67, CU68
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Catalog Controllers Integration Tests (CU27, CU30, CU32, CU35, CU36, CU38, CU40, CU58, CU65, CU67, CU68)")
class CatalogControllersIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Nested
    @DisplayName("EstadoDeGestionController - CU30/CU35/CU67")
    class EstadoDeGestionTests {

        @Test
        @DisplayName("CU67 - Should return 200 and list of estados de gestion")
        void shouldReturnAllEstadosDeGestion() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].nombre", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 200 for existing estado by ID")
        void shouldReturnEstadoDeGestionById() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idEstadoGestion", is(1)))
                    .andExpect(jsonPath("$.nombre", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 404 for non-existing estado")
        void shouldReturn404ForNonExistingEstado() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU30 - Should create new estado de gestion")
        void shouldCreateEstadoDeGestion() throws Exception {
            mockMvc.perform(post("/api/v1/estado-gestion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Archivada",
                                      "observaciones": "Gestion archivada",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU35 - Should update existing estado de gestion")
        void shouldUpdateEstadoDeGestion() throws Exception {
            mockMvc.perform(put("/api/v1/estado-gestion/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idEstadoGestion": 1,
                                      "nombre": "Iniciada - Modificada",
                                      "observaciones": "Actualizada",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("TipoDeDocumentoController - CU27/CU32/CU38/CU65")
    class TipoDeDocumentoTests {

        @Test
        @DisplayName("CU27 - Should return 200 and list of tipos de documento")
        void shouldReturnAllTiposDeDocumento() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-de-documento"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU27 - Should return 404 for non-existing tipo de documento")
        void shouldReturn404ForNonExistingTipoDocumento() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-de-documento/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU27 - Should create new tipo de documento")
        void shouldCreateTipoDeDocumento() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-de-documento")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Escritura de Venta",
                                      "vence": false,
                                      "quienEntrega": "Comprador",
                                      "devuelto": false,
                                      "habilitado": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU27 - Should return all tipos including newly created")
        void shouldReturnCreatedTipoDeDocumento() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-de-documento")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Poder Notarial",
                                      "vence": true,
                                      "diasVencimiento": 365,
                                      "quienEntrega": "Mandante",
                                      "habilitado": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());

            mockMvc.perform(get("/api/v1/tipo-de-documento"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.nombre == 'Poder Notarial')]", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("TipoDeFolioController - CU36/CU40/CU58/CU68")
    class TipoDeFolioTests {

        @Test
        @DisplayName("CU36 - Should return 200 and list of tipos de folio")
        void shouldReturnAllTiposDeFolio() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].nombre", notNullValue()));
        }

        @Test
        @DisplayName("CU36 - Should return tipo de folio by ID")
        void shouldReturnTipoDeFolioById() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idTipoFolio", is(1)));
        }

        @Test
        @DisplayName("CU36 - Should return 404 for non-existing tipo de folio")
        void shouldReturn404ForNonExistingTipoDeFolio() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU58 - Should create new tipo de folio")
        void shouldCreateTipoDeFolio() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-folio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "nombre": "Folio Especial",
                                      "observaciones": "Folio para documentos especiales",
                                      "habilitado": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU40 - Should update existing tipo de folio")
        void shouldUpdateTipoDeFolio() throws Exception {
            mockMvc.perform(put("/api/v1/tipo-folio/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idTipoFolio": 1,
                                      "nombre": "De documento - Actualizado",
                                      "habilitado": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CU40 - Should return 404 when updating non-existing tipo de folio")
        void shouldReturn404WhenUpdatingNonExistingTipoDeFolio() throws Exception {
            mockMvc.perform(put("/api/v1/tipo-folio/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idTipoFolio": 9999,
                                      "nombre": "Non Existing",
                                      "habilitado": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("TestimonioController - CU07/CU08/CU12/CU44")
    class TestimonioTests {

        @Test
        @DisplayName("CU07 - Should return 200 and empty list when no testimonios exist")
        void shouldReturnEmptyListOfTestimonios() throws Exception {
            mockMvc.perform(get("/api/v1/testimonio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU07 - Should return 404 for non-existing testimonio")
        void shouldReturn404ForNonExistingTestimonio() throws Exception {
            mockMvc.perform(get("/api/v1/testimonio/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("MovimientoTestimonioController - CU10/CU12/CU44")
    class MovimientoTestimonioTests {

        @Test
        @DisplayName("CU10 - Should return 200 and empty list when no movimientos exist")
        void shouldReturnEmptyListOfMovimientos() throws Exception {
            mockMvc.perform(get("/api/v1/movimiento-testimonio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU10 - Should return 404 for non-existing movimiento")
        void shouldReturn404ForNonExistingMovimiento() throws Exception {
            mockMvc.perform(get("/api/v1/movimiento-testimonio/9999"))
                    .andExpect(status().isNotFound());
        }
    }
}
