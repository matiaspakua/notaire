package com.licensis.notaire.business;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A JWT revoked by logout before its natural expiry (issue #676). Only the
 * token id ({@code jti}) is stored, never the token itself; the row is useless
 * once the token has expired and is purged after {@link #expiresAt}.
 */
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    protected RevokedToken() {
    }

    public RevokedToken(String jti, Instant expiresAt, Instant revokedAt) {
        this.jti = jti;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public String getJti() {
        return jti;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}
