-- =============================================================================
-- V42__add_observed_by_registry_to_testimony_movements.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-10-04
-- Description: Issue #851 / CU44 — records whether the Registro returned the
--              testimony observed when it is re-entered. Cartón number
--              (folder_number) and notes already exist on the table.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------
ALTER TABLE testimony_movements
    ADD COLUMN IF NOT EXISTS observed_by_registry BOOLEAN NOT NULL DEFAULT false;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM testimony_movements WHERE observed_by_registry;
