-- =============================================================================
-- V35__rename_testimony_and_substitution_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 9 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- testimonios and suplencias and their own columns to English, matching
-- the field names already used by the corresponding (already English)
-- JPA entities: Testimony, Substitution.
--
-- Columns not mapped by the JPA entity (pre-existing schema/entity drift,
-- unrelated to this rename) are translated too for table-wide
-- consistency, following the precedent set by
-- V25__rename_personas_to_people.sql.
--
-- fk_id_escritura on testimonios IS renamed (to fk_id_deed): its target
-- table (deeds) was already renamed in Slice 7. fk_id_suplantado and
-- fk_id_suplente on suplencias ARE renamed (to fk_id_substituted_person
-- and fk_id_substitute_person): they already point at "people" (V25).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: testimonios -> testimonies
ALTER TABLE testimonios RENAME TO testimonies;
ALTER TABLE testimonies RENAME COLUMN id_testimonio TO id;
ALTER TABLE testimonies RENAME COLUMN numero TO number;
ALTER TABLE testimonies RENAME COLUMN observaciones TO notes;
ALTER TABLE testimonies RENAME COLUMN fecha_inscripcion TO registration_date;
ALTER TABLE testimonies RENAME COLUMN fecha_retiro TO pickup_date;
ALTER TABLE testimonies RENAME COLUMN fecha_ingreso_libro TO book_entry_date;
ALTER TABLE testimonies RENAME COLUMN numero_carpeta TO folder_number;
ALTER TABLE testimonies RENAME COLUMN numero_expediente TO file_number;
ALTER TABLE testimonies RENAME COLUMN reingresado TO reentered;
ALTER TABLE testimonies RENAME COLUMN observado TO observed;
ALTER TABLE testimonies RENAME COLUMN verificado TO verified;
ALTER TABLE testimonies RENAME COLUMN fk_id_escritura TO fk_id_deed;
ALTER SEQUENCE IF EXISTS testimonios_id_testimonio_seq RENAME TO testimonies_id_seq;

-- Table: suplencias -> substitutions
ALTER TABLE suplencias RENAME TO substitutions;
ALTER TABLE substitutions RENAME COLUMN id_suplencia TO id;
ALTER TABLE substitutions RENAME COLUMN fecha_inicio TO start_date;
ALTER TABLE substitutions RENAME COLUMN fecha_fin TO end_date;
ALTER TABLE substitutions RENAME COLUMN observaciones TO notes;
ALTER TABLE substitutions RENAME COLUMN fk_id_suplantado TO fk_id_substituted_person;
ALTER TABLE substitutions RENAME COLUMN fk_id_suplente TO fk_id_substitute_person;
ALTER SEQUENCE IF EXISTS suplencias_id_suplencia_seq RENAME TO substitutions_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM testimonies;
-- SELECT COUNT(*) FROM substitutions;
