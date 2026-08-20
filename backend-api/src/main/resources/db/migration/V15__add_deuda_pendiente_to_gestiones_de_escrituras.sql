-- =============================================================================
-- V15__add_deuda_pendiente_to_gestiones_de_escrituras.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-08-19
-- Description: RF-22 requires warning about pending debt when a gestión is
--              archived, and RF-37 requires the archiving record to state
--              whether debt remained. Adds an additive, defaulted boolean
--              column so existing rows (and any caller that does not use the
--              new archive endpoint) are unaffected. See openspec/changes/
--              verify-debt-on-gestion-archive/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE gestiones_de_escrituras
    ADD COLUMN IF NOT EXISTS deuda_pendiente_al_archivar BOOLEAN;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name, is_nullable, data_type FROM information_schema.columns
--   WHERE table_name = 'gestiones_de_escrituras' AND column_name = 'deuda_pendiente_al_archivar';
-- Expect: 1 row, is_nullable = 'YES', data_type = 'boolean'.
