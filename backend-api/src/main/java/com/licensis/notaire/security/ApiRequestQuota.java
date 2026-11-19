package com.licensis.notaire.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fixed window of API calls for one key. The counters live in this JVM only
 * (issue #2010). Login lockout is a separate control.
 */
public final class ApiRequestQuota {

    private final int maxRequests;
    private final Duration window;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public ApiRequestQuota(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
    }

    public boolean allow(String key, Instant now) {
        Window current = windows.computeIfAbsent(key, ignored -> new Window(now));
        synchronized (current) {
            if (now.isAfter(current.start.plus(window)) || now.equals(current.start.plus(window))) {
                current.start = now;
                current.count = 0;
            }
            if (current.count >= maxRequests) {
                return false;
            }
            current.count++;
            return true;
        }
    }

    public long retryAfterSeconds(String key, Instant now) {
        Window current = windows.get(key);
        if (current == null) {
            return window.toSeconds();
        }
        synchronized (current) {
            long elapsed = Duration.between(current.start, now).getSeconds();
            return Math.max(1, window.toSeconds() - elapsed);
        }
    }

    private static final class Window {
        private Instant start;
        private int count;

        private Window(Instant start) {
            this.start = start;
        }
    }
}
