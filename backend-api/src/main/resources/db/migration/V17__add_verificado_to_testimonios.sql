-- =============================================================================
-- V17__add_verificado_to_testimonios.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-08-26
-- Description: CU08 - Verificar Testimonio needs to distinguish "not yet
--              verified" from "verified, not observed". The existing
--              `observado` boolean defaults to false, which is indistinguishable
--              from an actual not-observed verification outcome. Adds an
--              additive, non-null `verificado` flag (default false) so the
--              testimonio-movimiento-inscripcion circuit (CU11) can require a
--              verified testimonio before accepting it for inscripción. See
--              openspec/changes/escritura-post-firma-legal-cycle/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE testimonios
    ADD COLUMN IF NOT EXISTS verificado boolean NOT NULL DEFAULT false;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name, is_nullable, data_type, column_default
--   FROM information_schema.columns
--   WHERE table_name = 'testimonios' AND column_name = 'verificado';
-- Expect: 1 row, is_nullable = 'NO', data_type = 'boolean', column_default = 'false'.
