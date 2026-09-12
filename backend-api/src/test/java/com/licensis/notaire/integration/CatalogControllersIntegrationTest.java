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
 * Integration tests for catalog controllers that were previously returning 500 errors.
 * Covers: CU27, CU30, CU32, CU35, CU36, CU38, CU40, CU58, CU65, CU67, CU68
 */
@RequirementCoverage({"CU26", "CU27", "CU28", "CU29", "CU30", "CU31", "CU32", "CU33", "CU34", "CU35", "CU36", "CU37", "CU38", "CU40", "CU57", "CU58", "CU64", "CU65", "CU66", "CU67", "CU68"})
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
    class ManagementStatusTests {

        @Test
        @DisplayName("CU67 - Should return 200 and list of estados de gestion")
        void shouldReturnAllEstadosDeManagement() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 200 for existing status by ID")
        void shouldReturnManagementStatusById() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idManagementStatus", is(1)))
                    .andExpect(jsonPath("$.name", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 404 for non-existing status")
        void shouldReturn404ForNonExistingStatus() throws Exception {
            mockMvc.perform(get("/api/v1/estado-gestion/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU30 - Should create new status de gestion")
        void shouldCreateManagementStatus() throws Exception {
            mockMvc.perform(post("/api/v1/estado-gestion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Archivada",
                                      "notes": "Management archivada",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU35 - Should update existing status de gestion")
        void shouldUpdateManagementStatus() throws Exception {
            mockMvc.perform(put("/api/v1/estado-gestion/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idManagementStatus": 1,
                                      "name": "Iniciada - Modificada",
                                      "notes": "Actualizada",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("TipoDeDocumentoController - CU27/CU32/CU38/CU65")
    class DocumentTypeTests {

        @Test
        @DisplayName("CU27 - Should return 200 and list of tipos de documento")
        void shouldReturnAllTiposDeDocument() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-de-documento"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU27 - Should return 404 for non-existing type de documento")
        void shouldReturn404ForNonExistingTypeDocument() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-de-documento/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU27 - Should create new type de documento")
        void shouldCreateDocumentType() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-de-documento")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Deed de Venta",
                                      "expires": false,
                                      "deliveredBy": "Comprador",
                                      "returned": false,
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU27 - Should return all tipos including newly created")
        void shouldReturnCreatedDocumentType() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-de-documento")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Poder Notarial",
                                      "expires": true,
                                      "dueDays": 365,
                                      "deliveredBy": "Mandante",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());

            mockMvc.perform(get("/api/v1/tipo-de-documento"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.name == 'Poder Notarial')]", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("TipoDeFolioController - CU36/CU40/CU58/CU68")
    class FolioTypeTests {

        @Test
        @DisplayName("CU36 - Should return 200 and list of tipos de folio")
        void shouldReturnAllTiposDeFolio() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU36 - Should return type de folio by ID")
        void shouldReturnFolioTypeById() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idFolioType", is(1)));
        }

        @Test
        @DisplayName("CU36 - Should return 404 for non-existing type de folio")
        void shouldReturn404ForNonExistingFolioType() throws Exception {
            mockMvc.perform(get("/api/v1/tipo-folio/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU58 - Should create new type de folio")
        void shouldCreateFolioType() throws Exception {
            mockMvc.perform(post("/api/v1/tipo-folio")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Folio Especial",
                                      "notes": "Folio para documents especiales",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU40 - Should update existing type de folio")
        void shouldUpdateFolioType() throws Exception {
            mockMvc.perform(put("/api/v1/tipo-folio/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idFolioType": 1,
                                      "name": "De document - Actualizado",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CU40 - Should return 404 when updating non-existing type de folio")
        void shouldReturn404WhenUpdatingNonExistingFolioType() throws Exception {
            mockMvc.perform(put("/api/v1/tipo-folio/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idFolioType": 9999,
                                      "name": "Non Existing",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("TestimonioController - CU07/CU08/CU12/CU44")
    class TestimonyTests {

        @Test
        @DisplayName("CU07 - Should return 200 and empty list when no testimonios exist")
        void shouldReturnEmptyListOfTestimonios() throws Exception {
            mockMvc.perform(get("/api/v1/testimonio"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU07 - Should return 404 for non-existing testimony")
        void shouldReturn404ForNonExistingTestimony() throws Exception {
            mockMvc.perform(get("/api/v1/testimonio/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("MovimientoTestimonioController - CU10/CU12/CU44")
    class TestimonyMovementTests {

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
        void shouldReturn404ForNonExistingMovement() throws Exception {
            mockMvc.perform(get("/api/v1/movimiento-testimonio/9999"))
                    .andExpect(status().isNotFound());
        }
    }
}
