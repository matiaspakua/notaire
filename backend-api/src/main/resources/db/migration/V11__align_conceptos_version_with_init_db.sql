-- =============================================================================
-- V11__align_conceptos_version_with_init_db.sql
-- =============================================================================
-- Author: Claude
-- Date: 2026-06-19
-- Description: Align conceptos.version for "Documentacion" (id=3) with the
--              former init-db/02-data.sql value of 2, fixing the only data
--              discrepancy between the two schema sources. All other data and
--              schema differences were already covered by V3-V10. This is the
--              last step before removing init-db as a separate schema source.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- conceptos.id_concepto = 3 ("Documentacion"): version was 1 in V2 but 2 in
-- the old init-db/02-data.sql. This is the version field used for JPA
-- optimistic locking. Setting it to 2 ensures consistency when migrating
-- from the old init-db-provisioned databases.
UPDATE conceptos SET version = 2 WHERE id_concepto = 3 AND version = 1;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT version FROM conceptos WHERE id_concepto = 3; -- expect 2
