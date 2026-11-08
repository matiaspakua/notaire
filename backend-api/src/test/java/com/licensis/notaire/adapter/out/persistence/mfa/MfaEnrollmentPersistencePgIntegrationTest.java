package com.licensis.notaire.adapter.out.persistence.mfa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.licensis.notaire.application.port.out.mfa.MfaEnrollmentStorePort;
import com.licensis.notaire.domain.mfa.MfaEnrollment;
import com.licensis.notaire.integration.BaseIntegrationTest;
import com.licensis.notaire.testing.RequirementCoverage;

/** CU78 / ADR-032 — V54 migration and the store adapter against the real Flyway schema. */
@SpringBootTest
@ActiveProfiles("integration")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Tag("pg-integration")
@RequirementCoverage({"CU78"})
@DisplayName("MFA enrolment persistence against real Flyway schema (CU78)")
class MfaEnrollmentPersistencePgIntegrationTest extends BaseIntegrationTest {

    private static final String SECRET = "JBSWY3DPEHPK3PXP";

    @DynamicPropertySource
    static void mfaKey(DynamicPropertyRegistry registry) {
        registry.add("notaire.mfa.encryption-key", () -> "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=");
    }

    @Autowired
    private MfaEnrollmentStorePort store;

    @Autowired
    private JdbcTemplate jdbc;

    private int adminId() {
        return jdbc.queryForObject("SELECT id FROM users ORDER BY id LIMIT 1", Integer.class);
    }

    @Test
    @DisplayName("stores the secret as ciphertext and reads it back")
    void shouldEncryptAtRestAndRoundTrip() {
        int userId = adminId();
        store.save(new MfaEnrollment(userId, SECRET, true, 42, List.of("h1", "h2")));

        String raw = jdbc.queryForObject("SELECT secret_ciphertext FROM mfa_enrollments WHERE user_id = ?",
                String.class, userId);
        MfaEnrollment loaded = store.findByUserId(userId).orElseThrow();

        assertThat(raw).isNotBlank().doesNotContain(SECRET);
        assertThat(loaded.secretBase32()).isEqualTo(SECRET);
        assertThat(loaded.confirmed()).isTrue();
        assertThat(loaded.lastUsedStep()).isEqualTo(42);
        assertThat(loaded.recoveryCodeHashes()).containsExactly("h1", "h2");
    }

    @Test
    @DisplayName("save updates the existing row and delete removes it")
    void shouldUpdateAndDelete() {
        int userId = adminId();
        MfaEnrollment first = store.save(MfaEnrollment.begin(userId, SECRET));
        store.save(first.withLastUsedStep(7));

        assertThat(store.findByUserId(userId).orElseThrow().lastUsedStep()).isEqualTo(7);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mfa_enrollments WHERE user_id = ?", Integer.class,
                userId)).isEqualTo(1);

        store.deleteByUserId(userId);

        assertThat(store.findByUserId(userId)).isEmpty();
    }
}
