package com.licensis.notaire.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * Tracks failed login attempts per username and locks an account out for a
 * configurable duration once a configurable threshold is reached (issue #560).
 * The counters live in a {@link LoginAttemptStore}; in production that is the shared
 * database table, so the lock holds across backend instances and restarts (issue #1634).
 */
@Component
public class LoginAttemptService {

    private final int maxAttempts;
    private final long lockoutDurationMillis;
    private final Supplier<Instant> clock;
    private final LoginAttemptStore store;

    @Autowired
    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lockout-duration-ms:900000}") long lockoutDurationMillis,
            LoginAttemptStore store) {
        this(maxAttempts, lockoutDurationMillis, Instant::now, store);
    }

    LoginAttemptService(int maxAttempts, long lockoutDurationMillis, Supplier<Instant> clock) {
        this(maxAttempts, lockoutDurationMillis, clock, new InMemoryLoginAttemptStore());
    }

    public LoginAttemptService(int maxAttempts, long lockoutDurationMillis, Supplier<Instant> clock,
            LoginAttemptStore store) {
        this.maxAttempts = maxAttempts;
        this.lockoutDurationMillis = lockoutDurationMillis;
        this.clock = clock;
        this.store = store;
    }

    public boolean isLocked(String username) {
        String key = normalize(username);
        return store.lockedUntil(key).map(lockedUntil -> isStillLocked(key, lockedUntil)).orElse(false);
    }

    public void onLoginFailed(String username) {
        Instant now = clock.get();
        store.purgeIdleSince(now.minusMillis(lockoutDurationMillis));
        store.recordFailure(normalize(username), maxAttempts, now, now.plusMillis(lockoutDurationMillis));
    }

    public void onLoginSucceeded(String username) {
        store.clear(normalize(username));
    }

    private boolean isStillLocked(String key, Instant lockedUntil) {
        if (clock.get().isBefore(lockedUntil)) {
            return true;
        }
        store.clear(key);
        return false;
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
