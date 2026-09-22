-- =============================================================================
-- V30__rename_procedure_folder_and_registration_draft_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 5 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- carpetas_tramite and minutas_inscripcion and their own columns to
-- English, matching the field names already used by the corresponding
-- (already English) JPA entities: ProcedureFolder, RegistrationDraft.
--
-- fk_id_gestion and fk_id_tramite (on carpetas_tramite) and fk_id_escritura
-- (on minutas_inscripcion) are intentionally left untouched: they point at
-- gestiones_de_escrituras, tramites and escrituras respectively, none of
-- which are part of this slice (see tasks.md Slices 6-7).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: carpetas_tramite -> procedure_folders
ALTER TABLE carpetas_tramite RENAME TO procedure_folders;
ALTER TABLE procedure_folders RENAME COLUMN id_carpeta TO id;
ALTER TABLE procedure_folders RENAME COLUMN numero TO number;
ALTER TABLE procedure_folders RENAME COLUMN estado TO status;
ALTER TABLE procedure_folders RENAME COLUMN motivo_espera TO wait_reason;
ALTER SEQUENCE IF EXISTS carpetas_tramite_id_carpeta_seq RENAME TO procedure_folders_id_seq;

-- Table: minutas_inscripcion -> registration_drafts
ALTER TABLE minutas_inscripcion RENAME TO registration_drafts;
ALTER TABLE registration_drafts RENAME COLUMN id_minuta_inscripcion TO id;
ALTER TABLE registration_drafts RENAME COLUMN numero TO number;
ALTER TABLE registration_drafts RENAME COLUMN precio_operacion TO operation_price;
ALTER TABLE registration_drafts RENAME COLUMN estado TO status;
ALTER TABLE registration_drafts RENAME COLUMN fecha_generacion TO generation_date;
ALTER TABLE registration_drafts RENAME COLUMN fecha_presentacion TO submission_date;
ALTER TABLE registration_drafts RENAME COLUMN numero_entrada_registral TO registry_entry_number;
ALTER TABLE registration_drafts RENAME COLUMN fecha_recepcion TO reception_date;
ALTER TABLE registration_drafts RENAME COLUMN numero_inscripcion_definitivo TO final_registration_number;
ALTER TABLE registration_drafts RENAME COLUMN observaciones_registro TO registry_notes;
ALTER TABLE registration_drafts RENAME COLUMN fecha_subsanacion TO correction_date;
ALTER SEQUENCE IF EXISTS minutas_inscripcion_id_minuta_inscripcion_seq
    RENAME TO registration_drafts_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM procedure_folders;
-- SELECT COUNT(*) FROM registration_drafts;
