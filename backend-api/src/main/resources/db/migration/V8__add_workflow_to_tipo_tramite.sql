-- =============================================================================
-- V8__add_workflow_to_tipo_tramite.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-06-09
-- Description: Adds optional FK from tipos_de_tramite to workflow_definition
--              to support CU73 - Asignar workflow a tipo de tramite
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE tipos_de_tramite
    ADD COLUMN IF NOT EXISTS fk_workflow_definition_id integer
        REFERENCES workflow_definition(id_workflow_definition);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'tipos_de_tramite' AND column_name = 'fk_workflow_definition_id';
