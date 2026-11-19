package com.licensis.notaire.security;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One fixed-window API counter. The key is a username or a remote address, not a request body. */
@Entity
@Table(name = "api_rate_windows")
public class ApiRateWindow {

    @Id
    @Column(name = "bucket_key", nullable = false, length = 255)
    private String bucketKey;

    @Column(name = "window_start", nullable = false)
    private Instant windowStart;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    protected ApiRateWindow() {
    }

    public ApiRateWindow(String bucketKey, Instant windowStart) {
        this.bucketKey = bucketKey;
        this.windowStart = windowStart;
        this.requestCount = 1;
    }

    public Instant getWindowStart() {
        return windowStart;
    }

    public int getRequestCount() {
        return requestCount;
    }
}
