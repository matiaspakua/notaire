-- =============================================================================
-- V43__create_revoked_tokens.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-10-07
-- Description: Issue #676 — server-side JWT revocation on logout. Logout stores
--              the token id (jti claim) until the token's own expiry; the
--              authentication filter rejects any token whose jti is listed.
--              Only the id is stored, never the token. Expired rows are purged
--              by the application on the next logout.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS revoked_tokens (
    jti        VARCHAR(64)              NOT NULL PRIMARY KEY,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expires_at ON revoked_tokens (expires_at);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM revoked_tokens WHERE expires_at > now();
