-- =============================================================================
-- V36__rename_history_table_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 10 (final slice) of the remaining domain-schema-to-
-- English rename (#973, openspec/changes/translate-domain-schema-to-english).
-- Renames historial and its own columns to English, matching the field
-- names already used by the corresponding (already English) JPA entity:
-- History. historial has no incoming FK from any other table -- it is a
-- true leaf/terminal audit-log table.
--
-- fk_id_gestion and fk_id_estado_gestion ARE renamed here (to
-- fk_id_deed_management and fk_id_management_status): their target
-- tables (deed_managements, management_statuses) were already renamed in
-- Slices 7 and 1 respectively, so both sides are now consistently
-- English.
--
-- "fecha" is renamed to "event_date", not the literal "date": DATE is an
-- H2 reserved keyword, same class of issue as value/type (V27), year
-- (V32/V33) and budget_date (V34).
--
-- This is the last table-rename slice of the epic: every remaining
-- Spanish table/column identified in explore.md is now English. See
-- tasks.md for the final verification/close-out steps.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: historial -> history
ALTER TABLE historial RENAME TO history;
ALTER TABLE history RENAME COLUMN id_historial TO id;
ALTER TABLE history RENAME COLUMN fecha TO event_date;
ALTER TABLE history RENAME COLUMN observaciones TO notes;
ALTER TABLE history RENAME COLUMN fk_id_gestion TO fk_id_deed_management;
ALTER TABLE history RENAME COLUMN fk_id_estado_gestion TO fk_id_management_status;
ALTER SEQUENCE IF EXISTS historial_id_historial_seq RENAME TO history_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM history;
