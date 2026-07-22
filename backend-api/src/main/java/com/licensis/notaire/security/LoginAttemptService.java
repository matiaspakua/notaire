package com.licensis.notaire.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Tracks failed login attempts per username and locks an account out for a
 * configurable duration once a configurable threshold is reached (issue #560).
 * State is kept in-memory; this is a single-instance mitigation, not a
 * distributed rate limiter.
 */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final long lockoutDurationMillis;
    private final Supplier<Instant> clock;
    private final ConcurrentHashMap<String, Attempt> attemptsByUsername = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lockout-duration-ms:900000}") long lockoutDurationMillis) {
        this(maxAttempts, lockoutDurationMillis, Instant::now);
    }

    LoginAttemptService(int maxAttempts, long lockoutDurationMillis, Supplier<Instant> clock) {
        this.maxAttempts = maxAttempts;
        this.lockoutDurationMillis = lockoutDurationMillis;
        this.clock = clock;
    }

    public boolean isLocked(String username) {
        String key = normalize(username);
        Attempt attempt = attemptsByUsername.get(key);
        if (attempt == null || attempt.lockedUntil == null) {
            return false;
        }
        if (clock.get().isBefore(attempt.lockedUntil)) {
            return true;
        }
        attemptsByUsername.remove(key);
        return false;
    }

    public void onLoginFailed(String username) {
        String key = normalize(username);
        Attempt attempt = attemptsByUsername.computeIfAbsent(key, k -> new Attempt());
        attempt.failedCount++;
        if (attempt.failedCount >= maxAttempts) {
            attempt.lockedUntil = clock.get().plusMillis(lockoutDurationMillis);
        }
    }

    public void onLoginSucceeded(String username) {
        attemptsByUsername.remove(normalize(username));
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private static final class Attempt {
        private int failedCount;
        private Instant lockedUntil;
    }
}
