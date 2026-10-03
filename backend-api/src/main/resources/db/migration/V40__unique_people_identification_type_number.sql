-- =============================================================================
-- V40__unique_people_identification_type_number.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-03
-- Description: Issue #799 / CU17 / CU18 — enforce uniqueness of person
--              identification type + number at the database, matching the
--              service-layer rule shipped in #835. Fail fast when duplicate
--              groups already exist so operators can keep the lowest id before
--              retrying. Column fk_id_tipo_identificacion keeps its current
--              name (English rename deferred to a rename ADR).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Pre-check: refuse to add the unique index while duplicate groups remain
-- -----------------------------------------------------------------------------
DO $$
DECLARE
    duplicate_group_count integer;
BEGIN
    SELECT COUNT(*) INTO duplicate_group_count
    FROM (
        SELECT fk_id_tipo_identificacion, identification_number
        FROM people
        GROUP BY fk_id_tipo_identificacion, identification_number
        HAVING COUNT(*) > 1
    ) duplicate_groups;

    IF duplicate_group_count > 0 THEN
        RAISE EXCEPTION
            'Cannot enforce people identification uniqueness: % duplicate '
            '(fk_id_tipo_identificacion, identification_number) group(s) exist. '
            'Resolve duplicates (prefer keeping the lowest id) before retrying.',
            duplicate_group_count;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- Unique index matching #835 type + number rule
-- -----------------------------------------------------------------------------
CREATE UNIQUE INDEX IF NOT EXISTS uq_people_identification_type_number
    ON people (fk_id_tipo_identificacion, identification_number);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT indexname FROM pg_indexes
--   WHERE tablename = 'people' AND indexname = 'uq_people_identification_type_number';
-- Expect: 1 row.
