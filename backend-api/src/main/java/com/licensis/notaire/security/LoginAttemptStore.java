package com.licensis.notaire.security;

import java.time.Instant;
import java.util.Optional;

/**
 * Durable state behind {@link LoginAttemptService}: failed-login counters and lock expiry per
 * normalised username (issue #1634). Implementations must make {@code recordFailure} atomic so
 * concurrent failures on several instances add up.
 */
public interface LoginAttemptStore {

    Optional<Instant> lockedUntil(String username);

    /** Adds one failure at {@code now} and locks the user until {@code lockUntil} once {@code maxAttempts} is reached. */
    void recordFailure(String username, int maxAttempts, Instant now, Instant lockUntil);

    void clear(String username);

    /** Removes counters and locks whose last failure is older than {@code cutoff}. */
    void purgeIdleSince(Instant cutoff);
}
