package com.licensis.notaire.integration;

import com.licensis.notaire.business.DeedManagement;
import com.licensis.notaire.business.History;
import com.licensis.notaire.business.ManagementStatus;
import com.licensis.notaire.business.Person;
import com.licensis.notaire.business.User;
import com.licensis.notaire.config.JwtTokenService;
import com.licensis.notaire.repository.DeedManagementRepository;
import com.licensis.notaire.repository.HistoryRepository;
import com.licensis.notaire.repository.IdentificationTypeRepository;
import com.licensis.notaire.repository.ManagementStatusRepository;
import com.licensis.notaire.repository.PersonRepository;
import com.licensis.notaire.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The management history is the audit trail of state changes (CU13): only an administrator
 * may rewrite ({@code PUT}) or delete ({@code DELETE}) a history row (issue #1250, bucket B).
 * Reading and recording history stay open to any authenticated user.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@Transactional
@DisplayName("History writes are ADMIN-only (issue #1250)")
class HistoryWriteAuthorizationIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private UserRepository userRepository;
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
    private String employeeToken;
    private String administratorToken;
    private Integer managementId;
    private Integer historyId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        employeeToken = tokenForNewUser("EMPLEADO");
        administratorToken = tokenForNewUser("Escribano");

        Person person = new Person();
        person.setFirstName("Hist");
        person.setLastName("Auth");
        person.setIdentificationNumber("12500");
        person.setFkIdIdentificationType(identificationTypeRepository.findById(1).orElseThrow());
        person = personRepository.save(person);

        ManagementStatus status = statusRepository.findById(1).orElseThrow();
        DeedManagement management = new DeedManagement();
        management.setDateStart(new Date());
        management.setNumber(12500);
        management.setEncabezado("history authorization");
        management.setFkIdNotaryPerson(person);
        management.setFkIdManagementStatus(status);
        managementId = managementRepository.save(management).getIdManagement();

        History history = new History();
        history.setDate(new Date());
        history.setNotes("original");
        history.setFkIdManagement(management);
        history.setFkIdManagementStatus(status);
        historyId = historyRepository.save(history).getIdHistory();
    }

    private String tokenForNewUser(String type) {
        String name = "hist-" + UUID.randomUUID();
        userRepository.save(new User(null, name, "not-a-login-password", true, type));
        return jwtTokenService.generateToken(name);
    }

    private String body(String notes) {
        return "{\"notes\":\"" + notes + "\",\"managementStatusId\":1,\"managementId\":" + managementId + "}";
    }

    @Test
    @DisplayName("shouldForbidAnEmployeeFromRewritingHistory")
    void shouldForbidAnEmployeeFromRewritingHistory() throws Exception {
        mockMvc.perform(put("/api/v1/historial/{id}", historyId)
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("rewritten")))
                .andExpect(status().isForbidden());

        assertThat(historyRepository.findById(historyId).orElseThrow().getNotes()).isEqualTo("original");
    }

    @Test
    @DisplayName("shouldForbidAnEmployeeFromDeletingHistory")
    void shouldForbidAnEmployeeFromDeletingHistory() throws Exception {
        mockMvc.perform(delete("/api/v1/historial/{id}", historyId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isForbidden());

        assertThat(historyRepository.existsById(historyId)).isTrue();
    }

    @Test
    @DisplayName("shouldLetAnAdministratorRewriteHistory")
    void shouldLetAnAdministratorRewriteHistory() throws Exception {
        mockMvc.perform(put("/api/v1/historial/{id}", historyId)
                        .header("Authorization", "Bearer " + administratorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("corrected")))
                .andExpect(status().isOk());

        assertThat(historyRepository.findById(historyId).orElseThrow().getNotes()).isEqualTo("corrected");
    }

    @Test
    @DisplayName("shouldLetAnAdministratorDeleteHistory")
    void shouldLetAnAdministratorDeleteHistory() throws Exception {
        mockMvc.perform(delete("/api/v1/historial/{id}", historyId)
                        .header("Authorization", "Bearer " + administratorToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("shouldLetAnEmployeeReadAndRecordHistory")
    void shouldLetAnEmployeeReadAndRecordHistory() throws Exception {
        mockMvc.perform(get("/api/v1/historial/{id}", historyId)
                        .header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/historial").header("Authorization", "Bearer " + employeeToken))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/historial")
                        .header("Authorization", "Bearer " + employeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("recorded")))
                .andExpect(status().isCreated());
    }
}
