-- =============================================================================
-- V44__budget_status_vocabulary.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-10-10
-- Description: Issue #1346 (Owner default 2026-10-10) - budgets.status was free
--              text ("Pendiente" from the E2E fixtures, "Pending" from API
--              tests, "BORRADOR" from the UI). The vocabulary is now the codes
--              BORRADOR, PENDIENTE, APROBADO, RECHAZADO, FACTURADO, validated on
--              write by the API. Spanish and English spellings in any case map
--              to the code. Unknown values are kept, upper-cased and trimmed, so
--              no status is invented; the UI shows them as-is. Idempotent.
-- =============================================================================
UPDATE budgets
SET status = CASE UPPER(TRIM(status))
        WHEN 'PENDING'  THEN 'PENDIENTE'
        WHEN 'DRAFT'    THEN 'BORRADOR'
        WHEN 'APPROVED' THEN 'APROBADO'
        WHEN 'REJECTED' THEN 'RECHAZADO'
        WHEN 'INVOICED' THEN 'FACTURADO'
        ELSE UPPER(TRIM(status))
    END
WHERE status IS NOT NULL
  AND status IS DISTINCT FROM CASE UPPER(TRIM(status))
        WHEN 'PENDING'  THEN 'PENDIENTE'
        WHEN 'DRAFT'    THEN 'BORRADOR'
        WHEN 'APPROVED' THEN 'APROBADO'
        WHEN 'REJECTED' THEN 'RECHAZADO'
        WHEN 'INVOICED' THEN 'FACTURADO'
        ELSE UPPER(TRIM(status))
    END;
