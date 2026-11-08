-- =============================================================================
-- V54__create_mfa_enrollments.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-11-08
-- Description: #1640 / CU78 / ADR-032 — TOTP enrolment per user. The secret is stored
--              as AES-256-GCM ciphertext (never plaintext); recovery codes only as
--              BCrypt hashes, one per line; last_used_step gives replay protection.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS mfa_enrollments (
    user_id integer PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    secret_ciphertext text NOT NULL,
    confirmed boolean NOT NULL DEFAULT false,
    last_used_step bigint NOT NULL DEFAULT -1,
    recovery_code_hashes text NOT NULL DEFAULT '',
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT user_id, confirmed, last_used_step FROM mfa_enrollments;
