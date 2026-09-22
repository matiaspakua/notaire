-- =============================================================================
-- V31__rename_procedure_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 6 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- tramites and tramites_personas and their own columns to English,
-- matching the field names already used by the corresponding (already
-- English) JPA entities: Procedure, PersonProcedure.
--
-- fk_id_gestion, fk_id_escritura and fk_id_presupuesto on tramites are
-- intentionally left untouched: they point at gestiones_de_escrituras,
-- escrituras and presupuestos, none of which are part of this slice (see
-- tasks.md Slices 7-8).
--
-- fk_id_tipo_tramite and fk_id_inmueble on tramites ARE renamed (to
-- fk_id_procedure_type, fk_id_property): their target tables
-- (procedure_types, properties) were already renamed in Slice 1/2.
-- fk_id_tramite on tramites_personas is ALSO renamed (to fk_id_procedure)
-- since "tramites" is renamed to "procedures" in this very slice, and
-- fk_id_persona_cliente is renamed (to fk_id_client_person) since
-- "personas" was already renamed to "people" (V25).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: tramites -> procedures
ALTER TABLE tramites RENAME TO procedures;
ALTER TABLE procedures RENAME COLUMN id_tramite TO id;
ALTER TABLE procedures RENAME COLUMN numero TO number;
ALTER TABLE procedures RENAME COLUMN nombre TO name;
ALTER TABLE procedures RENAME COLUMN observaciones TO notes;
ALTER TABLE procedures RENAME COLUMN fk_id_tipo_tramite TO fk_id_procedure_type;
ALTER TABLE procedures RENAME COLUMN fk_id_inmueble TO fk_id_property;
ALTER SEQUENCE IF EXISTS tramites_id_tramite_seq RENAME TO procedures_id_seq;

-- Table: tramites_personas -> person_procedures
ALTER TABLE tramites_personas RENAME TO person_procedures;
ALTER TABLE person_procedures RENAME COLUMN fk_id_tramite TO fk_id_procedure;
ALTER TABLE person_procedures RENAME COLUMN fk_id_persona_cliente TO fk_id_client_person;
ALTER TABLE person_procedures RENAME COLUMN observaciones TO notes;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM procedures;
-- SELECT COUNT(*) FROM person_procedures;
