package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.ProcedureType;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.ProcedureTypeRepository;
import com.licensis.notaire.testing.RequirementCoverage;

/**
 * #806 — Residual History (audit log) gaps on plain create/update and
 * complete-case update, plus GET current-status entity-status fallback.
 */
@RequirementCoverage({"CU13", "CU02", "CU53"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("CU13 — orphan management status writes must populate History")
class ManagementHistoryOrphanWriteIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private ProcedureTypeRepository procedureTypeRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private DeedManagementRepository managementRepository;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    private Integer createPerson(String identificationNumber) throws Exception {
        String body = """
                {"firstName": "Notary", "lastName": "HistoryIT", "identificationNumber": "%s",
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
                {"number": 1, "date": "2026-01-01", "heading": "Budget History IT", "status": "PENDIENTE",
                 "person": {"personId": %d}}
                """.formatted(clientId);
        MvcResult result = mockMvc.perform(post("/api/v1/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString()).get("idBudget").asInt();
    }

    private ManagementStatus createStatus(String namePrefix) {
        ManagementStatus status = new ManagementStatus();
        status.setName(namePrefix + " " + System.nanoTime());
        return statusRepository.save(status);
    }

    private Integer createProcedureType() {
        ProcedureType type = new ProcedureType();
        type.setName("Procedure History IT " + System.nanoTime());
        type.setEnabled(true);
        type.setIsArchived(false);
        type.setIsRegistered(false);
        type.setAssociatesProperties(false);
        return procedureTypeRepository.save(type).getIdProcedureType();
    }

    private List<History> historyFor(Integer managementId) {
        return historyRepository.findByFkIdManagementIdManagement(managementId);
    }

    @Test
    @DisplayName("Plain create with status writes initial History")
    void shouldWriteHistoryOnPlainCreateWithStatus() throws Exception {
        Integer notaryId = createPerson("80610001");
        ManagementStatus status = createStatus("Initial");
        String body = """
                {"heading": "Plain create with status", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId, status.getIdManagementStatus());

        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        List<History> rows = historyFor(managementId);
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(status.getIdManagementStatus());
    }

    @Test
    @DisplayName("Plain create without status writes no History")
    void shouldNotWriteHistoryOnPlainCreateWithoutStatus() throws Exception {
        Integer notaryId = createPerson("80610002");
        String body = """
                {"heading": "Plain create without status", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId);

        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        assertThat(historyFor(managementId)).isEmpty();
    }

    @Test
    @DisplayName("Plain update that changes status is rejected (#804); History unchanged")
    void shouldRejectPlainUpdateStatusChangeWithoutWritingHistory() throws Exception {
        Integer notaryId = createPerson("80610003");
        ManagementStatus statusA = createStatus("A");
        ManagementStatus statusB = createStatus("B");
        String createBody = """
                {"heading": "Plain update change", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId, statusA.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();
        int before = historyFor(managementId).size();

        String updateBody = """
                {"heading": "Plain update change", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId, statusB.getIdManagementStatus());
        mockMvc.perform(put("/api/v1/managements/{id}", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isBadRequest());

        assertThat(historyFor(managementId)).hasSize(before);
        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(statusA.getIdManagementStatus());
    }

    @Test
    @DisplayName("Plain update that keeps status writes no History")
    void shouldNotWriteHistoryOnPlainUpdateSameStatus() throws Exception {
        Integer notaryId = createPerson("80610004");
        ManagementStatus status = createStatus("Same");
        int number = (int) (System.nanoTime() % 100000);
        String createBody = """
                {"heading": "Plain update same", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, status.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();
        int before = historyFor(managementId).size();

        String updateBody = """
                {"heading": "Plain update same renamed", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted(number, notaryId, status.getIdManagementStatus());
        mockMvc.perform(put("/api/v1/managements/{id}", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        assertThat(historyFor(managementId)).hasSize(before);
    }

    @Test
    @DisplayName("Complete-case update that changes status is rejected (#804); History unchanged")
    void shouldRejectCompleteCaseUpdateStatusChangeWithoutWritingHistory() throws Exception {
        Integer clientId = createPerson("80610005");
        Integer notaryId = createPerson("80610006");
        Integer budgetId = createBudget(clientId);
        ManagementStatus statusA = createStatus("CC-A");
        ManagementStatus statusB = createStatus("CC-B");
        Integer typeId = createProcedureType();
        int number = (int) (System.nanoTime() % 100000);

        String createBody = """
                {"number": %d, "heading": "Complete-case History", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, statusA.getIdManagementStatus(), typeId);
        MvcResult created = mockMvc.perform(post("/api/v1/managements/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();
        int before = historyFor(managementId).size();

        String updateBody = """
                {"number": %d, "heading": "Complete-case History", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, statusB.getIdManagementStatus(), typeId);
        mockMvc.perform(put("/api/v1/managements/{id}/complete-case", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isBadRequest());

        assertThat(historyFor(managementId)).hasSize(before);
        assertThat(managementRepository.findById(managementId))
                .isPresent()
                .get()
                .extracting(g -> g.getFkIdManagementStatus().getIdManagementStatus())
                .isEqualTo(statusA.getIdManagementStatus());
    }

    @Test
    @DisplayName("Complete-case update that keeps status writes no History")
    void shouldNotWriteHistoryOnCompleteCaseUpdateSameStatus() throws Exception {
        Integer clientId = createPerson("80610007");
        Integer notaryId = createPerson("80610008");
        Integer budgetId = createBudget(clientId);
        ManagementStatus status = createStatus("CC-Same");
        Integer typeId = createProcedureType();
        int number = (int) (System.nanoTime() % 100000);

        String createBody = """
                {"number": %d, "heading": "Complete-case same", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, status.getIdManagementStatus(), typeId);
        MvcResult created = mockMvc.perform(post("/api/v1/managements/complete-case")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();
        int before = historyFor(managementId).size();

        String updateBody = """
                {"number": %d, "heading": "Complete-case same renamed", "budgetId": %d,
                 "notaryId": %d, "statusManagementId": %d, "typeProcedureId": %d}
                """.formatted(number, budgetId, notaryId, status.getIdManagementStatus(), typeId);
        mockMvc.perform(put("/api/v1/managements/{id}/complete-case", managementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk());

        assertThat(historyFor(managementId)).hasSize(before);
    }

    @Test
    @DisplayName("current-status returns the latest history when rows exist")
    void shouldReturnCurrentStatusFromHistoryWhenRowsExist() throws Exception {
        Integer notaryId = createPerson("80610009");
        ManagementStatus statusA = createStatus("Hist-A");
        ManagementStatus statusB = createStatus("Hist-B");
        String createBody = """
                {"heading": "Status from history", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId, statusA.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        // #804 rejects status changes via PUT — append a second History row directly
        // (same effect as a successful /transition) to exercise current-status.
        var management = managementRepository.findById(managementId).orElseThrow();
        management.setFkIdManagementStatus(statusB);
        managementRepository.save(management);
        History second = new History();
        second.setFkIdManagement(management);
        second.setFkIdManagementStatus(statusB);
        second.setDate(new java.util.Date(System.currentTimeMillis() + 60_000L));
        historyRepository.save(second);

        mockMvc.perform(get("/api/v1/managements/{id}/current-status", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(managementId))
                .andExpect(jsonPath("$.statusManagementId").value(statusB.getIdManagementStatus()))
                .andExpect(jsonPath("$.statusManagementName").value(statusB.getName()))
                .andExpect(jsonPath("$.idHistory").isNumber());
    }

    @Test
    @DisplayName("GET current-status returns the later insert when history dates tie")
    void shouldReturnLaterHistoryRowWhenDatesTie() throws Exception {
        Integer notaryId = createPerson("HIST-TIE-" + System.nanoTime());
        ManagementStatus statusA = createStatus("Tie-A");
        ManagementStatus statusB = createStatus("Tie-B");
        String body = """
                {"heading": "Tie", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """;
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted((int) (System.nanoTime() % 100000), notaryId,
                                statusA.getIdManagementStatus())))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        // #804 rejects status changes via plain PUT — seed a second History row directly
        // so this tie-break assertion stays independent of workflow enforcement.
        var management = managementRepository.findById(managementId).orElseThrow();
        management.setFkIdManagementStatus(statusB);
        managementRepository.save(management);
        History second = new History();
        second.setFkIdManagement(management);
        second.setFkIdManagementStatus(statusB);
        second.setDate(new Date());
        historyRepository.saveAndFlush(second);

        List<History> rows = historyRepository.findByFkIdManagementIdManagement(managementId);
        assertThat(rows).hasSize(2);
        Date sameInstant = new Date(0L);
        rows.forEach(row -> row.setDate(sameInstant));
        historyRepository.saveAllAndFlush(rows);

        mockMvc.perform(get("/api/v1/managements/{id}/current-status", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusManagementId").value(statusB.getIdManagementStatus()));
    }

    @Test
    @DisplayName("current-status falls back to the entity status when history is empty")
    void shouldReturnCurrentStatusFallbackWhenHistoryEmpty() throws Exception {
        Integer notaryId = createPerson("80610010");
        ManagementStatus status = createStatus("Fallback");
        String body = """
                {"heading": "Status fallback", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d, "managementStatusId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId, status.getIdManagementStatus());
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        // Simulate a legacy row: entity has status but History was never written / was cleared.
        historyRepository.findByFkIdManagementIdManagement(managementId)
                .forEach(historyRepository::delete);
        historyRepository.flush();
        assertThat(historyFor(managementId)).isEmpty();
        assertThat(managementRepository.findById(managementId).orElseThrow().getFkIdManagementStatus())
                .isNotNull();

        mockMvc.perform(get("/api/v1/managements/{id}/current-status", managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.managementId").value(managementId))
                .andExpect(jsonPath("$.statusManagementId").value(status.getIdManagementStatus()))
                .andExpect(jsonPath("$.statusManagementName").value(status.getName()))
                .andExpect(jsonPath("$.idHistory").value(nullValue()));
    }

    @Test
    @DisplayName("current-status returns 404 when the management is missing")
    void shouldReturn404CurrentStatusWhenManagementMissing() throws Exception {
        mockMvc.perform(get("/api/v1/managements/{id}/current-status", 999999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("current-status returns 404 when the status is null and history is empty")
    void shouldReturn404CurrentStatusWhenStatusNullAndHistoryEmpty() throws Exception {
        Integer notaryId = createPerson("80610011");
        String body = """
                {"heading": "No status", "dateStart": "2026-01-01", "number": %d,
                 "notaryPersonId": %d}
                """.formatted((int) (System.nanoTime() % 100000), notaryId);
        MvcResult created = mockMvc.perform(post("/api/v1/managements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        Integer managementId = mapper.readTree(created.getResponse().getContentAsString())
                .get("idManagement").asInt();

        assertThat(historyFor(managementId)).isEmpty();
        assertThat(managementRepository.findById(managementId).orElseThrow().getFkIdManagementStatus())
                .isNull();

        mockMvc.perform(get("/api/v1/managements/{id}/current-status", managementId))
                .andExpect(status().isNotFound());
    }
}
