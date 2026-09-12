package com.licensis.notaire.integration;

import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for GET /api/v1/gestiones/{id}/historial.
 * Seeds Historial entries out of chronological order and asserts the
 * endpoint returns them sorted by date, per CU13.
 */
@RequirementCoverage({"CU13"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("GET /gestiones/{id}/history — returns the ordered bitácora (CU13)")
class ManagementBitacoraControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private PersonRepository personRepository;

    private MockMvc mockMvc;
    private Integer managementConHistoryId;
    private Integer managementSinHistoryId;
    private ManagementStatus statusInicial;
    private ManagementStatus statusFinal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        seedGestionesConHistory();
    }

    private ManagementStatus managementStatus(String name) {
        ManagementStatus status = new ManagementStatus();
        status.setName(name + " " + System.nanoTime());
        return statusRepository.save(status);
    }

    private Integer createManagement(ManagementStatus status) {
        DeedManagement management = new DeedManagement();
        management.setNumber((int) (System.nanoTime() % 100000));
        management.setEncabezado("Gestion Bitacora IT");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(status);
        management.setFkIdNotaryPerson(personRepository.findAll().get(0));
        return managementRepository.save(management).getIdManagement();
    }

    private void history(Integer idManagement, ManagementStatus status, long epochMillis) {
        History entry = new History();
        entry.setFkIdManagement(managementRepository.findById(idManagement).orElseThrow());
        entry.setFkIdManagementStatus(status);
        entry.setDate(new Date(epochMillis));
        historyRepository.save(entry);
    }

    private void seedGestionesConHistory() {
        statusInicial = managementStatus("Bitacora Inicial");
        statusFinal = managementStatus("Bitacora Final");

        managementConHistoryId = createManagement(statusFinal);
        long oneDayMillis = 86_400_000L;
        history(managementConHistoryId, statusFinal, 3 * oneDayMillis);
        history(managementConHistoryId, statusInicial, oneDayMillis);

        managementSinHistoryId = createManagement(statusInicial);
    }

    @Test
    @DisplayName("Query returns the full ordered history")
    void shouldReturnOrderedHistory() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/historial", managementConHistoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].statusManagementName").value(statusInicial.getName()))
                .andExpect(jsonPath("$[1].statusManagementName").value(statusFinal.getName()));
    }

    @Test
    @DisplayName("Should return an empty list when the gestión has no history entries")
    void shouldReturnEmptyListWhenNoHistoryEntries() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/{id}/historial", managementSinHistoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return 404 when the gestión does not exist")
    void shouldReturn404WhenManagementDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/gestiones/999999/historial"))
                .andExpect(status().isNotFound());
    }
}
