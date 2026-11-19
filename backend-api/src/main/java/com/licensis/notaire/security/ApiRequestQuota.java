package com.licensis.notaire.security;

import java.time.Duration;
import java.time.Instant;

/**
 * Fixed window of API calls for one key. Production counts in
 * {@link JpaApiRateWindowStore}, so every API instance shares the window
 * (issue #2010). Login lockout is a separate control.
 */
public final class ApiRequestQuota {

    private final int maxRequests;
    private final Duration window;
    private final ApiRateWindowStore store;

    public ApiRequestQuota(int maxRequests, Duration window) {
        this(maxRequests, window, new InMemoryApiRateWindowStore());
    }

    public ApiRequestQuota(int maxRequests, Duration window, ApiRateWindowStore store) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.store = store;
    }

    public boolean allow(String key, Instant now) {
        return store.tryAcquire(key, maxRequests, window, now);
    }

    public long retryAfterSeconds(String key, Instant now) {
        return store.retryAfterSeconds(key, window, now);
    }
}
