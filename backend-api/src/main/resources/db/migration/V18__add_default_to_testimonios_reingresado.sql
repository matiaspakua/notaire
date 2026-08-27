-- =============================================================================
-- V18__add_default_to_testimonios_reingresado.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-08-27
-- Description: `testimonios.reingresado` was created NOT NULL with no default
--              in V1 and is never mapped by the Testimonio JPA entity, so any
--              INSERT issued through TestimonioGeneracionVerificacionService
--              (CU07 - Generar Testimonio) omits the column and fails with
--              SQLState 23502 (not-null violation). Adds a default so the
--              entity's inserts succeed; existing rows are unaffected since the
--              column was already NOT NULL and always populated until now.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE testimonios
    ALTER COLUMN reingresado SET DEFAULT false;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name, is_nullable, column_default
--   FROM information_schema.columns
--   WHERE table_name = 'testimonios' AND column_name = 'reingresado';
-- Expect: 1 row, is_nullable = 'NO', column_default = 'false'.
