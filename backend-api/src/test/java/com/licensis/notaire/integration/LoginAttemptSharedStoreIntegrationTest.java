package com.licensis.notaire.integration;

import com.licensis.notaire.security.JpaLoginAttemptStore;
import com.licensis.notaire.security.LoginAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("Login lockout state is shared and durable (issue #1634)")
class LoginAttemptSharedStoreIntegrationTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_MS = 60_000L;

    @Autowired
    private JpaLoginAttemptStore store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Instant now;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM login_attempts");
        now = Instant.parse("2026-01-01T00:00:00Z");
    }

    private LoginAttemptService newInstance() {
        return new LoginAttemptService(MAX_ATTEMPTS, LOCKOUT_MS, () -> now, store);
    }

    private void failThreeTimes(LoginAttemptService service, String username) {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            service.onLoginFailed(username);
        }
    }

    @Test
    @DisplayName("A lock set through one instance applies to another instance")
    void shouldApplyALockAcrossInstances() {
        LoginAttemptService first = newInstance();
        LoginAttemptService second = newInstance();

        failThreeTimes(first, "Admin");

        assertThat(second.isLocked("admin")).isTrue();
    }

    @Test
    @DisplayName("Failures spread across instances add up to the threshold")
    void shouldCountFailuresAcrossInstances() {
        LoginAttemptService first = newInstance();
        LoginAttemptService second = newInstance();

        first.onLoginFailed("admin");
        second.onLoginFailed("admin");
        first.onLoginFailed("admin");

        assertThat(second.isLocked("admin")).isTrue();
    }

    @Test
    @DisplayName("A restart does not reset the lock")
    void shouldKeepTheLockAfterARestart() {
        failThreeTimes(newInstance(), "admin");

        assertThat(newInstance().isLocked("admin")).isTrue();
    }

    @Test
    @DisplayName("A successful login clears the counter on every instance")
    void shouldClearTheCounterOnSuccess() {
        LoginAttemptService first = newInstance();
        LoginAttemptService second = newInstance();
        first.onLoginFailed("admin");
        first.onLoginFailed("admin");

        second.onLoginSucceeded("admin");
        first.onLoginFailed("admin");

        assertThat(first.isLocked("admin")).isFalse();
    }

    @Test
    @DisplayName("The lock expires after the lock duration")
    void shouldUnlockAfterTheLockDuration() {
        LoginAttemptService service = newInstance();
        failThreeTimes(service, "admin");

        now = now.plusMillis(LOCKOUT_MS + 1);

        assertThat(service.isLocked("admin")).isFalse();
    }

    @Test
    @DisplayName("Expired and idle rows of other users are purged when a failure is recorded")
    void shouldPurgeExpiredEntries() {
        LoginAttemptService service = newInstance();
        failThreeTimes(service, "locked-user");
        service.onLoginFailed("idle-user");

        now = now.plusMillis(LOCKOUT_MS + 1);
        service.onLoginFailed("someone-else");

        List<String> usernames = jdbcTemplate.queryForList("SELECT username FROM login_attempts", String.class);
        assertThat(usernames).containsExactly("someone-else");
    }

    @Test
    @DisplayName("The table holds nothing but the username, counters and timestamps")
    void shouldStoreNoPersonalDataBeyondTheUsername() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_name) = 'login_attempts'",
                String.class);

        assertThat(columns).containsExactlyInAnyOrder("username", "failed_count", "locked_until", "updated_at");
    }
}
