package com.licensis.notaire.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginAttemptService")
class LoginAttemptServiceTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION_MS = 1000L;

    private Instant now;
    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        now = Instant.parse("2026-01-01T00:00:00Z");
        service = new LoginAttemptService(MAX_ATTEMPTS, LOCKOUT_DURATION_MS, () -> now);
    }

    @Test
    @DisplayName("Should not lock a user before reaching the max failed attempts")
    void shouldNotLockBeforeMaxFailedAttempts() {
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");

        assertThat(service.isLocked("admin")).isFalse();
    }

    @Test
    @DisplayName("Should lock a user after reaching the max failed attempts")
    void shouldLockAfterMaxFailedAttempts() {
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");

        assertThat(service.isLocked("admin")).isTrue();
    }

    @Test
    @DisplayName("Should treat usernames case-insensitively")
    void shouldTreatUsernamesCaseInsensitively() {
        service.onLoginFailed("Admin");
        service.onLoginFailed("ADMIN");
        service.onLoginFailed("admin");

        assertThat(service.isLocked("aDmIn")).isTrue();
    }

    @Test
    @DisplayName("Should not lock unrelated usernames")
    void shouldNotLockUnrelatedUsernames() {
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");

        assertThat(service.isLocked("otheruser")).isFalse();
    }

    @Test
    @DisplayName("Should unlock automatically once the lockout duration has elapsed")
    void shouldUnlockAfterLockoutDurationElapses() {
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");
        assertThat(service.isLocked("admin")).isTrue();

        now = now.plusMillis(LOCKOUT_DURATION_MS + 1);

        assertThat(service.isLocked("admin")).isFalse();
    }

    @Test
    @DisplayName("Should reset the failed attempt counter after a successful login")
    void shouldResetFailedCountOnSuccessfulLogin() {
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");
        service.onLoginSucceeded("admin");
        service.onLoginFailed("admin");
        service.onLoginFailed("admin");

        assertThat(service.isLocked("admin")).isFalse();
    }
}
