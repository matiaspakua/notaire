package com.licensis.notaire.security;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Single-JVM store used by unit tests; production uses {@link JpaLoginAttemptStore}. */
public class InMemoryLoginAttemptStore implements LoginAttemptStore {

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    public Optional<Instant> lockedUntil(String username) {
        Attempt attempt = attempts.get(username);
        return attempt == null ? Optional.empty() : Optional.ofNullable(attempt.lockedUntil);
    }

    @Override
    public void recordFailure(String username, int maxAttempts, Instant now, Instant lockUntil) {
        attempts.compute(username, (key, current) -> {
            Attempt attempt = current == null ? new Attempt() : current;
            attempt.failedCount++;
            attempt.updatedAt = now;
            if (attempt.failedCount >= maxAttempts) {
                attempt.lockedUntil = lockUntil;
            }
            return attempt;
        });
    }

    @Override
    public void clear(String username) {
        attempts.remove(username);
    }

    @Override
    public void purgeIdleSince(Instant cutoff) {
        attempts.values().removeIf(attempt -> attempt.updatedAt.isBefore(cutoff));
    }

    private static final class Attempt {
        private int failedCount;
        private Instant lockedUntil;
        private Instant updatedAt;
    }
}
