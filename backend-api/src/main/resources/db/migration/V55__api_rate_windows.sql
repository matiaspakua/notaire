-- =============================================================================
-- V55__api_rate_windows.sql
-- =============================================================================
-- Author: Cursor Agent
-- Date: 2026-11-19
-- Description: Issue #2010 / CU78 / SR-15. API quota windows move from one
--              JVM's memory to a table shared by every backend instance.
--              Only the bucket key (username or remote address), a counter
--              and the window start are stored. Rollback: DROP TABLE api_rate_windows.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS api_rate_windows (
    bucket_key    VARCHAR(255)             PRIMARY KEY,
    window_start  TIMESTAMP WITH TIME ZONE NOT NULL,
    request_count INTEGER                  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_api_rate_windows_window_start ON api_rate_windows (window_start);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- testing/database/checks/71_api_rate_windows.sql
