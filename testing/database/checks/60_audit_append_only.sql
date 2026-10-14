\set QUIET on
-- Audit rows are append-only (migration V47, issue #1636, SR-09). Everything runs in a
-- transaction that is rolled back, so the probe row never persists (it could not be deleted).
BEGIN;

CREATE TEMP TABLE vv_audit_results (line text) ON COMMIT DROP;

INSERT INTO audit_records (version, date, module, operation_detail)
VALUES (0, now(), 'VV', 'append-only probe');

INSERT INTO vv_audit_results
SELECT CASE WHEN count(*) = 1 THEN 'PASS' ELSE 'FAIL' END || ' audit insert and select still work'
FROM audit_records WHERE module = 'VV' AND operation_detail = 'append-only probe';

DO $$
BEGIN
    BEGIN
        UPDATE audit_records SET operation_detail = 'rewritten' WHERE module = 'VV';
        INSERT INTO vv_audit_results VALUES ('FAIL audit update was accepted');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO vv_audit_results VALUES (CASE WHEN SQLERRM ILIKE '%append-only%'
            THEN 'PASS audit update is rejected' ELSE 'FAIL audit update failed for another reason: ' || SQLERRM END);
    END;
    BEGIN
        DELETE FROM audit_records WHERE module = 'VV';
        INSERT INTO vv_audit_results VALUES ('FAIL audit delete was accepted');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO vv_audit_results VALUES (CASE WHEN SQLERRM ILIKE '%append-only%'
            THEN 'PASS audit delete is rejected' ELSE 'FAIL audit delete failed for another reason: ' || SQLERRM END);
    END;
    BEGIN
        TRUNCATE audit_records;
        INSERT INTO vv_audit_results VALUES ('FAIL audit truncate was accepted');
    EXCEPTION WHEN OTHERS THEN
        INSERT INTO vv_audit_results VALUES (CASE WHEN SQLERRM ILIKE '%append-only%'
            THEN 'PASS audit truncate is rejected' ELSE 'FAIL audit truncate failed for another reason: ' || SQLERRM END);
    END;
END $$;

SELECT line FROM vv_audit_results ORDER BY line DESC;

ROLLBACK;
