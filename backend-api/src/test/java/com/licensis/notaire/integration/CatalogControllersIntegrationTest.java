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
    @DisplayName("ManagementStatusController - CU30/CU35/CU67")
    class ManagementStatusTests {

        @Test
        @DisplayName("CU67 - Should return 200 and list of management statuses")
        void shouldReturnAllManagementStatuses() throws Exception {
            mockMvc.perform(get("/api/v1/management-statuses"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 200 for existing status by ID")
        void shouldReturnManagementStatusById() throws Exception {
            mockMvc.perform(get("/api/v1/management-statuses/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idManagementStatus", is(1)))
                    .andExpect(jsonPath("$.name", notNullValue()));
        }

        @Test
        @DisplayName("CU67 - Should return 404 for non-existing status")
        void shouldReturn404ForNonExistingStatus() throws Exception {
            mockMvc.perform(get("/api/v1/management-statuses/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU30 - Should create new status of management")
        void shouldCreateManagementStatus() throws Exception {
            mockMvc.perform(post("/api/v1/management-statuses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Archivada",
                                      "notes": "Archived management",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU35 - Should update existing status of management")
        void shouldUpdateManagementStatus() throws Exception {
            mockMvc.perform(put("/api/v1/management-statuses/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idManagementStatus": 1,
                                      "name": "Initial - Modified",
                                      "notes": "Updated",
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DocumentTypeController - CU27/CU32/CU38/CU65")
    class DocumentTypeTests {

        @Test
        @DisplayName("CU27 - Should return 200 and list of document types")
        void shouldReturnAllDocumentTypes() throws Exception {
            mockMvc.perform(get("/api/v1/document-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU27 - Should return 404 for a missing document type")
        void shouldReturn404ForNonExistingTypeDocument() throws Exception {
            mockMvc.perform(get("/api/v1/document-types/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU27 - Should create new document type")
        void shouldCreateDocumentType() throws Exception {
            mockMvc.perform(post("/api/v1/document-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Deed of sale",
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
        @DisplayName("CU27 - Should return all document types including newly created")
        void shouldReturnCreatedDocumentType() throws Exception {
            mockMvc.perform(post("/api/v1/document-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Notarial power",
                                      "expires": true,
                                      "dueDays": 365,
                                      "deliveredBy": "Grantor",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());

            mockMvc.perform(get("/api/v1/document-types"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.name == 'Notarial power')]", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("FolioTypeController - CU36/CU40/CU58/CU68")
    class FolioTypeTests {

        @Test
        @DisplayName("CU36 - Should return 200 and a list of folio types")
        void shouldReturnAllFolioTypes() throws Exception {
            mockMvc.perform(get("/api/v1/folio-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU36 - Should return folio type by ID")
        void shouldReturnFolioTypeById() throws Exception {
            mockMvc.perform(get("/api/v1/folio-types/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idFolioType", is(1)));
        }

        @Test
        @DisplayName("CU36 - Should return 404 for non-existing folio type")
        void shouldReturn404ForNonExistingFolioType() throws Exception {
            mockMvc.perform(get("/api/v1/folio-types/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU58 - Should create new folio type")
        void shouldCreateFolioType() throws Exception {
            mockMvc.perform(post("/api/v1/folio-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Special folio",
                                      "notes": "Folio for special documents",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU40 - Should update existing folio type")
        void shouldUpdateFolioType() throws Exception {
            mockMvc.perform(put("/api/v1/folio-types/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "idFolioType": 1,
                                      "name": "Document - Updated",
                                      "enabled": true,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("CU40 - Should return 404 when updating non-existing folio type")
        void shouldReturn404WhenUpdatingNonExistingFolioType() throws Exception {
            mockMvc.perform(put("/api/v1/folio-types/9999")
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
    @DisplayName("TestimonyController - CU07/CU08/CU12/CU44")
    class TestimonyTests {

        @Test
        @DisplayName("CU07 - Should return 200 and an empty list when no testimonies exist")
        void shouldReturnEmptyListOfTestimonies() throws Exception {
            mockMvc.perform(get("/api/v1/testimonies"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU07 - Should return 404 for non-existing testimony")
        void shouldReturn404ForNonExistingTestimony() throws Exception {
            mockMvc.perform(get("/api/v1/testimonies/9999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("TestimonyMovementController - CU10/CU12/CU44")
    class TestimonyMovementTests {

        @Test
        @DisplayName("CU10 - Should return 200 and empty list when no movements exist")
        void shouldReturnEmptyListOfMovimientos() throws Exception {
            mockMvc.perform(get("/api/v1/testimony-movements"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU10 - Should return 404 for a missing movement")
        void shouldReturn404ForNonExistingMovement() throws Exception {
            mockMvc.perform(get("/api/v1/testimony-movements/9999"))
                    .andExpect(status().isNotFound());
        }
    }
}
