package com.licensis.notaire.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.licensis.notaire.security.JpaApiRateWindowStore;

@SpringBootTest
@ActiveProfiles("test-h2")
@DisplayName("JpaApiRateWindowStore handles concurrent first requests without race conditions")
class JpaApiRateWindowStoreConcurrencyTest {

    private static final int THREADS = 32;
    private static final int ROUNDS = 4;

    @Autowired
    private JpaApiRateWindowStore store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM api_rate_windows");
    }

    @Test
    @DisplayName("concurrent first requests on a fresh key are all counted, none throw")
    void concurrentFirstRequestsAllCounted() throws Exception {
        Instant now = Instant.parse("2026-11-19T12:00:00Z");
        Duration window = Duration.ofSeconds(60);

        for (int round = 0; round < ROUNDS; round++) {
            String key = "concurrent-key-round-" + round;
            jdbcTemplate.update("DELETE FROM api_rate_windows WHERE bucket_key = ?", key);

            CountDownLatch ready = new CountDownLatch(THREADS);
            CountDownLatch go = new CountDownLatch(1);
            AtomicReference<Throwable> firstError = new AtomicReference<>();
            AtomicInteger successCount = new AtomicInteger(0);

            try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
                for (int t = 0; t < THREADS; t++) {
                    executor.submit(() -> {
                        try {
                            ready.countDown();
                            go.await(10, TimeUnit.SECONDS);
                            boolean result = store.tryAcquire(key, 1000, window, now);
                            if (result) {
                                successCount.incrementAndGet();
                            }
                        } catch (Throwable ex) {
                            firstError.compareAndSet(null, ex);
                        }
                    });
                }

                ready.await(10, TimeUnit.SECONDS);
                go.countDown();
                executor.shutdown();
                executor.awaitTermination(30, TimeUnit.SECONDS);
            }

            Throwable error = firstError.get();
            if (error != null) {
                failWithCause("round " + round + ": concurrent first requests threw an exception", error);
            }

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT request_count FROM api_rate_windows WHERE bucket_key = ?",
                    Integer.class, key);

            assertThat(count)
                    .as("round %d: all %d concurrent requests should be counted (got %d)", round, THREADS, count)
                    .isEqualTo(THREADS);
            assertThat(successCount.get())
                    .as("round %d: all %d requests should succeed (got %d)", round, THREADS, successCount.get())
                    .isEqualTo(THREADS);
        }
    }

    private void failWithCause(String message, Throwable cause) {
        assertThat(cause).as(message).isNull();
    }
}
