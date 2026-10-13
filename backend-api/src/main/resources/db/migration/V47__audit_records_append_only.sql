-- =============================================================================
-- V47__audit_records_append_only.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-10-13
-- Description: Issue #1636 / CU78 / SR-09. Makes audit_records append-only at the
--              database level: a trigger rejects row UPDATE and DELETE and statement
--              TRUNCATE, and the privileges are revoked from PUBLIC and from the
--              migrating role. INSERT and SELECT are untouched.
--              Retention/purge: only the table owner, by disabling the trigger
--              (ALTER TABLE audit_records DISABLE TRIGGER USER) in a reviewed
--              maintenance window, then enabling it again.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE OR REPLACE FUNCTION reject_audit_records_change() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'audit_records is append-only: % is not allowed', TG_OP
        USING ERRCODE = 'insufficient_privilege';
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS audit_records_append_only ON audit_records;
CREATE TRIGGER audit_records_append_only
    BEFORE UPDATE OR DELETE ON audit_records
    FOR EACH ROW EXECUTE FUNCTION reject_audit_records_change();

DROP TRIGGER IF EXISTS audit_records_no_truncate ON audit_records;
CREATE TRIGGER audit_records_no_truncate
    BEFORE TRUNCATE ON audit_records
    FOR EACH STATEMENT EXECUTE FUNCTION reject_audit_records_change();

REVOKE UPDATE, DELETE, TRUNCATE ON audit_records FROM PUBLIC;
DO $$
BEGIN
    EXECUTE format('REVOKE UPDATE, DELETE, TRUNCATE ON audit_records FROM %I', current_user);
END $$;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- testing/database/checks/60_audit_append_only.sql
