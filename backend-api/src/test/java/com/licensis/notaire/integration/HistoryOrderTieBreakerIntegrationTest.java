package com.licensis.notaire.integration;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.testing.RequirementCoverage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * #1383: every history read path returns one total order — event date ascending,
 * then idHistory ascending (insertion order) for entries on the same day.
 */
@RequirementCoverage({"CU13"})
@SpringBootTest
@Transactional
@ActiveProfiles("test-h2")
@DisplayName("#1383 History read paths order by date, then idHistory (CU13)")
class HistoryOrderTieBreakerIntegrationTest {

    private static final long ONE_DAY_MILLIS = 86_400_000L;

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
    private Integer managementId;
    private ManagementStatus later;
    private ManagementStatus sameDayFirst;
    private ManagementStatus sameDaySecond;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        later = newStatus("Order Later");
        sameDayFirst = newStatus("Order SameDay 1");
        sameDaySecond = newStatus("Order SameDay 2");

        DeedManagement management = new DeedManagement();
        management.setNumber((int) (System.nanoTime() % 100000));
        management.setEncabezado("History order IT");
        management.setDateStart(new Date());
        management.setFkIdManagementStatus(later);
        management.setFkIdNotaryPerson(personRepository.findAll().get(0));
        managementId = managementRepository.save(management).getIdManagement();

        // Inserted out of date order; the two same-day rows share the exact instant
        // (event_date is a SQL DATE, so the time of day is lost on write).
        history(later, 3 * ONE_DAY_MILLIS);
        history(sameDayFirst, ONE_DAY_MILLIS);
        history(sameDaySecond, ONE_DAY_MILLIS);
    }

    private ManagementStatus newStatus(String name) {
        ManagementStatus status = new ManagementStatus();
        status.setName(name + " " + System.nanoTime());
        return statusRepository.save(status);
    }

    private void history(ManagementStatus status, long epochMillis) {
        History entry = new History();
        entry.setFkIdManagement(managementRepository.findById(managementId).orElseThrow());
        entry.setFkIdManagementStatus(status);
        entry.setDate(new Date(epochMillis));
        historyRepository.saveAndFlush(entry);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/api/v1/managements/{id}/history", "/api/v1/history/management/{id}"})
    @DisplayName("returns date ascending, same-day entries in insertion order")
    void returnsTotalOrder(String path) throws Exception {
        mockMvc.perform(get(path, managementId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].statusManagementName").value(sameDayFirst.getName()))
                .andExpect(jsonPath("$[1].statusManagementName").value(sameDaySecond.getName()))
                .andExpect(jsonPath("$[2].statusManagementName").value(later.getName()));
    }
}
