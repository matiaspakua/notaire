package com.licensis.notaire.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/** Single-JVM window used by unit tests. Production uses {@link JpaApiRateWindowStore}. */
public final class InMemoryApiRateWindowStore implements ApiRateWindowStore {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, int maxRequests, Duration window, Instant now) {
        Window current = windows.computeIfAbsent(bucket(key), ignored -> new Window(now));
        synchronized (current) {
            if (!now.isBefore(current.start.plus(window))) {
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

    @Override
    public long retryAfterSeconds(String key, Duration window, Instant now) {
        Window current = windows.get(bucket(key));
        if (current == null) {
            return window.toSeconds();
        }
        synchronized (current) {
            long elapsed = Duration.between(current.start, now).getSeconds();
            return Math.max(1, window.toSeconds() - elapsed);
        }
    }

    static String bucket(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }
        return key.length() <= 255 ? key : key.substring(0, 255);
    }

    private static final class Window {
        private Instant start;
        private int count;

        private Window(Instant start) {
            this.start = start;
        }
    }
}
