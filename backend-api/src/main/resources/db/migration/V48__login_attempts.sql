-- =============================================================================
-- V48__login_attempts.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-10-13
-- Description: Issue #1634 / CU78 / SR-01. Failed-login counters and lock expiry
--              move from one JVM's memory to a table shared by every backend
--              instance. Only the normalised username, a counter and timestamps
--              are stored. Rollback: DROP TABLE login_attempts.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS login_attempts (
    username     VARCHAR(255)             PRIMARY KEY,
    failed_count INTEGER                  NOT NULL DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_login_attempts_updated_at ON login_attempts (updated_at);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- testing/database/checks/70_login_attempts.sql
