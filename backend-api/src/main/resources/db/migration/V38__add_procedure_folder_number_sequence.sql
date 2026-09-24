-- =============================================================================
-- V38__add_procedure_folder_number_sequence.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-24
-- Description: Issue #1038 (CU85). Procedure folder numbers were computed in
-- the application as max(number) + 1, so two concurrent case creations read
-- the same max and the second insert violated uq_carpeta_tramite_numero.
-- Numbers now come from this sequence, which PostgreSQL hands out atomically.
-- The sequence starts after the highest number already in use.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------
CREATE SEQUENCE IF NOT EXISTS procedure_folder_number_seq AS integer;

SELECT setval('procedure_folder_number_seq',
              COALESCE((SELECT MAX(number) FROM procedure_folders), 0) + 1,
              false);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT last_value, is_called FROM procedure_folder_number_seq;
