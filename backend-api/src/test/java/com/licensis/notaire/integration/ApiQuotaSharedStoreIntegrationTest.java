package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.licensis.notaire.security.ApiRequestQuota;
import com.licensis.notaire.security.JpaApiRateWindowStore;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("API quota state is shared across instances (issue #2010)")
class ApiQuotaSharedStoreIntegrationTest {

    private static final Duration WINDOW = Duration.ofSeconds(60);

    @Autowired
    private JpaApiRateWindowStore store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM api_rate_windows");
    }

    private ApiRequestQuota quota() {
        return new ApiRequestQuota(2, WINDOW, store);
    }

    @Test
    @DisplayName("a burst counted on one instance rejects the next instance")
    void burstIsShared() {
        Instant start = Instant.parse("2026-11-19T12:00:00Z");
        ApiRequestQuota first = quota();
        ApiRequestQuota second = quota();

        assertThat(first.allow("alice", start)).isTrue();
        assertThat(first.allow("alice", start.plusSeconds(1))).isTrue();
        assertThat(second.allow("alice", start.plusSeconds(2))).isFalse();
        assertThat(second.retryAfterSeconds("alice", start.plusSeconds(2))).isEqualTo(58);
        assertThat(second.allow("bob", start.plusSeconds(2))).isTrue();
    }

    @Test
    @DisplayName("a new instance still enforces the window, then allows the next one")
    void restartKeepsTheWindow() {
        Instant start = Instant.parse("2026-11-19T12:00:00Z");
        assertThat(quota().allow("alice", start)).isTrue();
        assertThat(quota().allow("alice", start.plusSeconds(1))).isTrue();
        assertThat(quota().allow("alice", start.plusSeconds(2))).isFalse();
        assertThat(quota().allow("alice", start.plusSeconds(60))).isTrue();
    }

    @Test
    @DisplayName("the table holds only the bucket key, the window start, and the count")
    void columnsStayMinimal() {
        quota().allow("alice", Instant.parse("2026-11-19T12:00:00Z"));
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT LOWER(column_name) FROM information_schema.columns WHERE LOWER(table_name) = 'api_rate_windows'",
                String.class);
        assertThat(columns).containsExactlyInAnyOrder("bucket_key", "window_start", "request_count");
    }
}
