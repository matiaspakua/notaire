package com.licensis.notaire.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/** Failed-login counter and lock expiry for one normalised username (issue #1634). */
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected LoginAttempt() {
    }

    public LoginAttempt(String username, Instant now) {
        this.username = username;
        this.updatedAt = now;
    }

    public void recordFailure(int maxAttempts, Instant now, Instant lockUntil) {
        failedCount++;
        updatedAt = now;
        if (failedCount >= maxAttempts) {
            lockedUntil = lockUntil;
        }
    }

    public String getUsername() {
        return username;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
