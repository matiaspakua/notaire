package com.licensis.notaire.integration;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code GET /api/v1/historial} is paginated (slice of issue #596): the history table
 * grows with every state change, so the list answers a Spring Data page bounded by
 * {@code size} instead of the whole table. Each test seeds its own rows and rolls back.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("History list pagination (issue #596)")
class HistoryPaginationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private HistoryRepository historyRepository;
    @Autowired
    private DeedManagementRepository managementRepository;
    @Autowired
    private ManagementStatusRepository statusRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private IdentificationTypeRepository identificationTypeRepository;

    private MockMvc mockMvc;
    private Integer newestId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        Person person = new Person();
        person.setFirstName("Pag");
        person.setLastName("Historial");
        person.setIdentificationNumber("59610");
        person.setFkIdIdentificationType(identificationTypeRepository.findById(1).orElseThrow());
        person = personRepository.save(person);

        ManagementStatus status = statusRepository.findById(1).orElseThrow();
        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setNumber(59610);
        management.setEncabezado("pagination");
        management.setFkIdNotaryPerson(person);
        management.setFkIdManagementStatus(status);
        management = managementRepository.save(management);

        for (int i = 0; i < 3; i++) {
            History history = new History();
            history.setDate(new Date());
            history.setNotes("pagination " + i);
            history.setFkIdManagement(management);
            history.setFkIdManagementStatus(status);
            newestId = historyRepository.save(history).getIdHistory();
        }
    }

    @Test
    @DisplayName("shouldReturnRequestedPageSize")
    void shouldReturnRequestedPageSize() throws Exception {
        mockMvc.perform(get("/api/v1/historial").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("shouldDefaultToTwentyPerPage")
    void shouldDefaultToTwentyPerPage() throws Exception {
        mockMvc.perform(get("/api/v1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("shouldSortNewestFirstWhenAsked")
    void shouldSortNewestFirstWhenAsked() throws Exception {
        mockMvc.perform(get("/api/v1/historial").param("size", "1").param("sort", "idHistory,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].idHistory").value(newestId))
                .andExpect(jsonPath("$.content[0].notes").value("pagination 2"))
                .andExpect(jsonPath("$.content[0].statusManagementId").value(1));
    }
}
