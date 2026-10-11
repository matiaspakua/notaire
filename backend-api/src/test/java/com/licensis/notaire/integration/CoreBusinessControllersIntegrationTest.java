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
    @DisplayName("PersonController - CU17/CU18/CU41/CU54/CU61")
    class PersonControllerTests {

        @Test
        @DisplayName("CU18 - Should return all people")
        void shouldReturnAllPeople() throws Exception {
            mockMvc.perform(get("/api/v1/people"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU18 - Should return person by ID")
        void shouldReturnPersonById() throws Exception {
            mockMvc.perform(get("/api/v1/people/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.personId", is(1)));
        }

        @Test
        @DisplayName("CU18 - Should return 404 for non-existing person")
        void shouldReturn404ForNonExistingPerson() throws Exception {
            mockMvc.perform(get("/api/v1/people/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU17 - Should create new person (client)")
        void shouldCreatePerson() throws Exception {
            mockMvc.perform(post("/api/v1/people")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "firstName": "Carlos",
                                      "lastName": "Gomez",
                                      "identificationNumber": "30987654",
                                      "isClient": true,
                                      "fkIdIdentificationType": { "idIdentificationType": 1 },
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("CU41 - Should search people by firstName")
        void shouldSearchPeopleByFirstName() throws Exception {
            mockMvc.perform(get("/api/v1/people/search")
                            .param("firstName", "Juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU61 - Should search people by identificationNumber")
        void shouldSearchPeopleByIdentificationNumber() throws Exception {
            mockMvc.perform(get("/api/v1/people/search")
                            .param("identificationNumber", "20123456"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }
    }

    @Nested
    @DisplayName("ConceptController - CU29/CU34/CU37/CU66")
    class ConceptControllerTests {

        @Test
        @DisplayName("CU66 - Should return all conceptos")
        void shouldReturnAllConcepts() throws Exception {
            mockMvc.perform(get("/api/v1/concepts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU66 - Should return concept by ID")
        void shouldReturnConceptById() throws Exception {
            mockMvc.perform(get("/api/v1/concepts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idConcept", is(1)));
        }

        @Test
        @DisplayName("CU66 - Should return 404 for non-existing concept")
        void shouldReturn404ForNonExistingConcept() throws Exception {
            mockMvc.perform(get("/api/v1/concepts/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU29 - Should create new concept")
        void shouldCreateConcept() throws Exception {
            mockMvc.perform(post("/api/v1/concepts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Honorario adicional",
                                      "value": 5000,
                                      "percentage": 0,
                                      "enabled": true,
                                      "fixedConcept": false,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("BudgetController - CU01/CU45/CU60")
    class BudgetControllerTests {

        @Test
        @DisplayName("CU45 - Should return all budgets")
        void shouldReturnAllBudgets() throws Exception {
            mockMvc.perform(get("/api/v1/budgets"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU45 - Should return budget by ID")
        void shouldReturnBudgetById() throws Exception {
            mockMvc.perform(get("/api/v1/budgets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.idBudget", is(1)));
        }

        @Test
        @DisplayName("CU45 - Should return 404 for non-existing budget")
        void shouldReturn404ForNonExistingBudget() throws Exception {
            mockMvc.perform(get("/api/v1/budgets/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU01 - Should create new budget")
        void shouldCreateBudget() throws Exception {
            mockMvc.perform(post("/api/v1/budgets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 20250099,
                                      "date": "2025-06-01",
                                      "heading": "Budget test",
                                      "status": "PENDIENTE",
                                      "propertyAmount": 50000.00,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("DeedController - CU05/CU06/CU52/CU62")
    class DeedControllerTests {

        @Test
        @DisplayName("CU06 - Should return all deeds")
        void shouldReturnAllDeeds() throws Exception {
            mockMvc.perform(get("/api/v1/deeds"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU06 - Should return 404 for non-existing deed")
        void shouldReturn404ForNonExistingDeed() throws Exception {
            mockMvc.perform(get("/api/v1/deeds/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("CU05 - Should return available notaries")
        void shouldReturnAvailableNotaries() throws Exception {
            mockMvc.perform(get("/api/v1/deeds/available-notaries"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("UserController - CU20/CU21/CU23")
    class UserControllerTests {

        @Test
        @DisplayName("CU20 - Should return all users")
        void shouldReturnAllUsers() throws Exception {
            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU20 - Should login with admin credentials")
        void shouldLoginWithAdminCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "admin",
                                      "password": "admin"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid", is(true)));
        }

        @Test
        @DisplayName("CU20 - Should reject invalid credentials")
        void shouldRejectInvalidCredentials() throws Exception {
            mockMvc.perform(post("/api/v1/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "admin",
                                      "password": "wrong_password"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.valid", is(false)));
        }
    }

    @Nested
    @DisplayName("ProcedureTypeController - CU26/CU31/CU57/CU64")
    class ProcedureTypeControllerTests {

        @Test
        @DisplayName("CU64 - Should return all procedure types")
        void shouldReturnAllProcedureTypes() throws Exception {
            mockMvc.perform(get("/api/v1/procedure-types"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)))
                    .andExpect(jsonPath("$[0].name", notNullValue()));
        }

        @Test
        @DisplayName("CU26 - Should create new type of procedure")
        void shouldCreateProcedureType() throws Exception {
            mockMvc.perform(post("/api/v1/procedure-types")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "name": "Donacion",
                                      "notes": "Procedure de donacion",
                                      "enabled": true,
                                      "isArchived": true,
                                      "isRegistered": false,
                                      "associatesProperties": false,
                                      "version": 0
                                    }
                                    """))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("AuditRecordController - CU23")
    class AuditRecordControllerTests {

        @Test
        @DisplayName("CU23 - Should return all audit records")
        void shouldReturnAllRegistrosAudit() throws Exception {
            mockMvc.perform(get("/api/v1/audit-log"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU23 - Should return 200 for audit by user")
        void shouldReturnAuditByUser() throws Exception {
            mockMvc.perform(get("/api/v1/audit-log/user/1"))
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
            mockMvc.perform(get("/api/v1/folios"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("SubstitutionController - CU22/CU59")
    class SubstitutionControllerTests {

        @Test
        @DisplayName("CU59 - Should return all substitutions")
        void shouldReturnAllSubstitutions() throws Exception {
            mockMvc.perform(get("/api/v1/substitutions"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }
    }

    @Nested
    @DisplayName("PropertyController")
    class PropertyControllerTests {

        @Test
        @DisplayName("Should return all properties")
        void shouldReturnAllProperties() throws Exception {
            mockMvc.perform(get("/api/v1/properties"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", isA(java.util.List.class)));
        }

        @Test
        @DisplayName("CU82 - Should save matricula, tomo/folios/finca and linderos")
        void shouldSaveRegistrationNumberVolumeFolioLandRecordYBoundaries() throws Exception {
            String body = """
                    {
                      "cadastralDesignation": "123-456-789",
                      "address": "Calle Falsa 123",
                      "fiscalAppraisal": 1000.00,
                      "registrationNumber": "M-1",
                      "volumeFolioLandRecord": "T1-F2-FN3",
                      "boundaries": "Norte, Sur, Este, Oeste"
                    }
                    """;

            mockMvc.perform(post("/api/v1/properties")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.registrationNumber", is("M-1")))
                    .andExpect(jsonPath("$.volumeFolioLandRecord", is("T1-F2-FN3")))
                    .andExpect(jsonPath("$.boundaries", is("Norte, Sur, Este, Oeste")));
        }
    }
}
