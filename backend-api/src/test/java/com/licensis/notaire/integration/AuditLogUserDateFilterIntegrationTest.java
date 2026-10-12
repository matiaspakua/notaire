package com.licensis.notaire.integration;

import com.licensis.notaire.business.AuditRecord;
import com.licensis.notaire.business.User;
import com.licensis.notaire.repository.AuditRecordRepository;
import com.licensis.notaire.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * #1545 / CU23: GET /api/v1/audit-log filters by user and an inclusive date range
 * ({@code from}/{@code to}, yyyy-MM-dd, business time zone), combined with the
 * module filter, paged. A bad date or {@code from > to} answers 400.
 */
@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Audit log: user and date-range filter (#1545)")
class AuditLogUserDateFilterIntegrationTest {

    private static final String MODULE = "Audit1545";
    private static final LocalDate DAY = LocalDate.of(2031, 5, 15);

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private AuditRecordRepository auditRepository;
    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private final List<Integer> created = new ArrayList<>();
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        List<User> users = userRepository.findAll();
        assertThat(users).isNotEmpty();
        userA = users.get(0);
        userB = new User();
        userB.setName("audit1545-" + System.nanoTime());
        userB.setPassword(userA.getPassword());
        userB.setStatus(true);
        userB.setType(userA.getType());
        userB.setRole(userA.getRole());
        userB.setFkIdPerson(userA.getFkIdPerson());
        userB = userRepository.save(userB);
        record(userA, DAY.minusDays(2).atTime(12, 0), "a-before");
        record(userA, DAY.minusDays(1).atStartOfDay(), "a-from-start");
        record(userA, DAY.atTime(23, 59, 59), "a-to-end");
        record(userA, DAY.plusDays(1).atStartOfDay(), "a-after");
        record(userB, DAY.atTime(10, 0), "b-in-range");
    }

    @AfterEach
    void tearDown() {
        auditRepository.deleteAllById(created);
        created.clear();
        userRepository.delete(userB);
    }

    private void record(User user, LocalDateTime when, String detail) {
        AuditRecord r = new AuditRecord(null, detail, Date.from(when.atZone(ZoneId.systemDefault()).toInstant()));
        r.setModule(MODULE);
        r.setFkIdUser(user);
        created.add(auditRepository.save(r).getIdAuditRecord());
    }

    @Test
    @DisplayName("user + inclusive range + module: only that user's records within the range")
    void filtersByUserAndInclusiveRange() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20")
                        .param("module", MODULE)
                        .param("idUser", String.valueOf(userA.getIdUser()))
                        .param("from", DAY.minusDays(1).toString())
                        .param("to", DAY.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].operationDetail").value("a-to-end"))
                .andExpect(jsonPath("$.content[1].operationDetail").value("a-from-start"));
    }

    @Test
    @DisplayName("module + user combine (the module no longer overrides the user)")
    void moduleAndUserCombine() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20")
                        .param("module", MODULE)
                        .param("idUser", String.valueOf(userB.getIdUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].operationDetail").value("b-in-range"));
    }

    @Test
    @DisplayName("only from, or only to, is an open-ended range")
    void openEndedRange() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20").param("module", MODULE)
                        .param("from", DAY.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20").param("module", MODULE)
                        .param("to", DAY.minusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("a malformed date answers 400")
    void badDateIs400() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20").param("from", "15/05/2031"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("from after to answers 400")
    void fromAfterToIs400() throws Exception {
        mockMvc.perform(get("/api/v1/audit-log")
                        .param("page", "0").param("size", "20")
                        .param("from", DAY.toString()).param("to", DAY.minusDays(1).toString()))
                .andExpect(status().isBadRequest());
    }
}
