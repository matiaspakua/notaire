-- =============================================================================
-- V14__drop_presupuestos_fk_id_tramite.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-08-14
-- Description: Presupuesto and Tramite declared two independent, contradictory
--              relations to each other: presupuestos.fk_id_tramite (one
--              Presupuesto -> at most one Tramite) and tramites.fk_id_presupuesto
--              (one Presupuesto -> many Tramites, via Presupuesto.tramiteList).
--              Only the latter is written by the live, modern write path
--              (GestionController.applyTramiteDependencies, CU02 "Iniciar
--              Gestion"). This migration removes the unused
--              presupuestos.fk_id_tramite column and its FK constraint so the
--              schema matches the single, canonical relation now expressed by
--              the entity model. See openspec/changes/
--              resolve-presupuesto-tramite-cardinality/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Fail loudly rather than silently discard data: the report found this column
-- latent on the live write path, but that is not the same guarantee as "empty
-- in every environment."
DO $$
DECLARE
    orphaned_rows integer;
BEGIN
    SELECT COUNT(*) INTO orphaned_rows FROM presupuestos WHERE fk_id_tramite IS NOT NULL;
    IF orphaned_rows > 0 THEN
        RAISE EXCEPTION
            'V14 aborted: % row(s) in presupuestos.fk_id_tramite are non-null. '
            'Review and resolve these rows by hand before dropping the column '
            '(see openspec/changes/resolve-presupuesto-tramite-cardinality/design.md).',
            orphaned_rows;
    END IF;
END $$;

ALTER TABLE presupuestos DROP CONSTRAINT IF EXISTS fk_presupuestos_tramite;

ALTER TABLE presupuestos DROP COLUMN IF EXISTS fk_id_tramite;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'presupuestos' AND column_name = 'fk_id_tramite';
-- Expect: 0 rows.
