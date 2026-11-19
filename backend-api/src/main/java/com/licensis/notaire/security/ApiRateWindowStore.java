package com.licensis.notaire.security;

import java.time.Duration;
import java.time.Instant;

/**
 * Fixed-window counter behind {@link ApiRequestQuota}. A production store must
 * make {@code tryAcquire} atomic so several API instances share one count
 * (issue #2010).
 */
public interface ApiRateWindowStore {

    boolean tryAcquire(String key, int maxRequests, Duration window, Instant now);

    long retryAfterSeconds(String key, Duration window, Instant now);
}
