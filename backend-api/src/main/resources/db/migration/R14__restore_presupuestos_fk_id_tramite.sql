-- =============================================================================
-- R14__restore_presupuestos_fk_id_tramite.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-08-14
-- Description: Rollback script for V14__drop_presupuestos_fk_id_tramite.sql.
--              Not run automatically by Flyway (does not match its versioned or
--              repeatable naming convention by design) — apply by hand only if
--              reverting the entity-model change alongside this schema change.
--              Restores the column nullable, with no data: V14 verified the
--              column held no non-null rows before dropping it, so there is
--              nothing to backfill.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration
-- -----------------------------------------------------------------------------

ALTER TABLE presupuestos ADD COLUMN IF NOT EXISTS fk_id_tramite integer;

ALTER TABLE presupuestos ADD CONSTRAINT fk_presupuestos_tramite
    FOREIGN KEY (fk_id_tramite) REFERENCES tramites(id_tramite);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'presupuestos' AND column_name = 'fk_id_tramite';
-- Expect: 1 row.
