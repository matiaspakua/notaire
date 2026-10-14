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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test-h2")
@TestPropertySource(properties = "security.audit.list-read-window-seconds=0")
@DisplayName("Reads of personal data are audited without PII (issue #1635)")
class AuditPersonalDataReadIntegrationTest {

    private static final String DNI = "98765432";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AuditRecordRepository auditRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        User admin = userRepository.findAll().stream()
                .filter(u -> "admin".equalsIgnoreCase(u.getName()))
                .findFirst()
                .orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                admin.getName(), "n/a", AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("A people search writes exactly one audit row that holds no search term")
    void shouldWriteExactlyOneRowWithoutTheSearchTerm() throws Exception {
        long before = auditRepository.count();

        mockMvc.perform(get("/api/v1/people/search").param("identificationNumber", DNI))
                .andExpect(status().isOk());

        List<AuditRecord> records = auditRepository.findAll();
        assertThat(records).hasSize((int) before + 1);
        AuditRecord latest = records.stream().max((a, b) -> Integer.compare(a.getIdAuditRecord(), b.getIdAuditRecord()))
                .orElseThrow();
        assertThat(latest.getOperationDetail()).contains("/api/v1/people/search").doesNotContain(DNI);
    }

    @Test
    @DisplayName("A people list writes exactly one audit row")
    void shouldWriteExactlyOneRowOnList() throws Exception {
        long before = auditRepository.count();

        mockMvc.perform(get("/api/v1/people")).andExpect(status().isOk());

        assertThat(auditRepository.count()).isEqualTo(before + 1);
    }
}
