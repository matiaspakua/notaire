package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.Substitution;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.business.Procedure;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.SubstitutionRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.repository.ProcedureRepository;
import com.licensis.notaire.testing.RequirementCoverage;

import java.util.Calendar;
import java.util.Date;

@SpringBootTest
@ActiveProfiles("test-h2")
@RequirementCoverage({"CU02", "CU22"})
@DisplayName("Gestion controller — create validates data before hitting the database")
class ManagementControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ManagementStatusRepository managementStatusRepository;

    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;

    @Autowired
    private ProcedureRepository procedureRepository;

    @Autowired
    private DeedManagementRepository deedManagementRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private SubstitutionRepository substitutionRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson() throws Exception {
        return createPerson("420" + (System.nanoTime() % 100000));
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary IT", "lastName": "Management IT", "identificationNumber": "%s",
                 "isClient": false, "identificationType": {"idIdentificationType": 1}}
                """.formatted(identificationNumber);
        MvcResult result = mockMvc.perform(post("/api/v1/people")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("personId").asInt();
    }

    private Integer createBudget(Integer clientId) throws Exception {
        String body = """
                {"number": 1, "date": "2026-01-01", "encabezado": "Budget IT", "status": "Pending",
                 "person": {"personId": %d}}
                """.formatted(clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/presupuestos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private Integer createManagementStatus() {
        ManagementStatus status = new ManagementStatus();
        status.setName("Estado IT");
        return managementStatusRepository.save(status).getIdManagementStatus();
    }

    private Integer createProcedureType() {
        ProcedureType type = new ProcedureType();
        type.setName("Tramite IT");
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    @Test
    @DisplayName("Should return 400 with a body, not a bare 500, when encabezado is missing")
    void shouldReturn400WhenEncabezadoIsMissing() throws Exception {
        Integer personId = createPerson();
        String body = """
                {"dateStart": "2026-01-01", "number": 9101,
                 "fkIdNotaryPerson": {"personId": %d}}
                """.formatted(personId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("Should return 201 when creating a gestion with all required fields")
    void shouldCreateManagementWithValidData() throws Exception {
        Integer personId = createPerson();
        String body = """
                {"encabezado": "Management IT", "dateStart": "2026-01-01", "number": 9102,
                 "fkIdNotaryPerson": {"personId": %d}}
                """.formatted(personId);

        mockMvc.perform(post("/api/v1/gestiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idManagement").isNumber());
    }

    @Test
    @DisplayName("Should return 400 from complete-case when a required dependency is missing")
    void shouldReturn400FromCompleteCaseWhenDependencyIsMissing() throws Exception {
        String body = """
                {"number": 9201, "encabezado": "Management IT"}
                """;

        mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should create a gestion with its tramite when all case dependencies are provided")
    void shouldCreateCompleteCaseWithValidDependencies() throws Exception {
        Integer clientId = createPerson("42000010");
        Integer notaryId = createPerson("42000011");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        String body = """
                {"number": 9202, "encabezado": "Management IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);

        MvcResult result = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idManagement").isNumber())
                .andReturn();
        Integer managementId = mapper.readTree(result.getResponse().getContentAsString()).get("idManagement").asInt();

        List<Procedure> procedures = procedureRepository.findByFkIdManagementIdManagement(managementId);
        assertThat(procedures).as("complete-case should persist a tramite linked to the gestion").hasSize(1);
        assertThat(procedures.get(0).getFkIdBudget())
                .as("the persisted tramite should carry the requested presupuestoId as its fkIdPresupuesto")
                .isNotNull()
                .extracting("idBudget")
                .isEqualTo(budgetId);
    }

    @Test
    @DisplayName("Should return 404 when updating complete-case for a gestion that does not exist")
    void shouldReturn404WhenUpdatingCompleteCaseForMissingManagement() throws Exception {
        Integer clientId = createPerson("42000012");
        Integer notaryId = createPerson("42000013");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        String body = """
                {"number": 9203, "encabezado": "Management IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);

        mockMvc.perform(put("/api/v1/gestiones/999999/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update a gestion and its tramite when all case dependencies are provided")
    void shouldUpdateCompleteCaseWithValidDependencies() throws Exception {
        Integer clientId = createPerson("42000014");
        Integer notaryId = createPerson("42000015");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        String createBody = """
                {"number": 9204, "encabezado": "Management IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString()).get("idManagement").asInt();

        String updateBody = """
                {"number": 9204, "encabezado": "Management IT actualizada", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);

        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idManagement").value(managementId));
    }

    @Test
    @DisplayName("Should redirect a gestion to the suplente when the requested notary has an active suplencia")
    void shouldRedirectToSuplenteWhenUpdatingManagementNotary() throws Exception {
        Integer clientId = createPerson("42000016");
        Integer notaryId = createPerson("42000017");
        Integer suplenteId = createPerson("42000018");
        Integer budgetId = createBudget(clientId);
        Integer statusId = createManagementStatus();
        Integer typeProcedureId = createProcedureType();
        String createBody = """
                {"number": 9205, "encabezado": "Management IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);
        MvcResult created = mockMvc.perform(post("/api/v1/gestiones/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString()).get("idManagement").asInt();
        createActiveSubstitution(notaryId, suplenteId);

        String updateBody = """
                {"number": 9205, "encabezado": "Management IT", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(budgetId, notaryId, statusId, typeProcedureId);
        mockMvc.perform(put("/api/v1/gestiones/" + managementId + "/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        DeedManagement management = deedManagementRepository.findById(managementId).orElseThrow();
        Person suplente = personRepository.findById(suplenteId).orElseThrow();
        assertThat(management.getFkIdNotaryPerson().getPersonId())
                .as("the gestion should be redirected to the suplente, not the requested notary")
                .isEqualTo(suplenteId);
        assertThat(management.getNotes())
                .as("the redirection should be recorded, identifying both escribanos")
                .contains(suplente.getFirstName())
                .contains(suplente.getLastName());
    }

    private void createActiveSubstitution(Integer notaryId, Integer suplenteId) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date dateStart = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 2);
        Date dateEnd = calendar.getTime();

        Substitution substitution = new Substitution(null, dateStart, dateEnd);
        substitution.setFkIdSubstituted(personRepository.findById(notaryId).orElseThrow());
        substitution.setFkIdSubstitute(personRepository.findById(suplenteId).orElseThrow());
        substitutionRepository.save(substitution);
    }
}
