-- =============================================================================
-- V26__rename_leaf_reference_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 1 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames the
-- low-fan-out leaf/reference tables and their own columns to English,
-- matching the field names already used by the corresponding (already
-- English) JPA entities: IdentificationType, DocumentType, FolioType,
-- ProcedureType, Role, ManagementStatus, AuditRecord, Notebook.
--
-- FK columns on OTHER tables that reference these tables (e.g.
-- fk_id_tipo_documento on documentos_presentados, fk_id_estado_gestion on
-- gestiones_de_escrituras/historial, fk_id_rol on usuarios) are
-- intentionally left untouched here -- they belong to the owning entity's
-- own future rename slice, per the epic's vertical-slice strategy
-- (precedent set by V25__rename_personas_to_people.sql). PostgreSQL
-- automatically keeps FK constraints valid across a table/column rename.
--
-- registro_auditoria.fk_id_usuario is also left untouched: it is this
-- table's own column, but it points at "usuarios", which is not part of
-- this slice; renaming it now would create an English column name
-- pointing at a still-Spanish table, adding confusion instead of removing
-- it.
--
-- NOTE: "identificaciones" (JPA entity Identification.java) has no
-- corresponding Flyway-managed table -- it is pre-existing schema/entity
-- drift unrelated to this rename (the entity was migrated from the legacy
-- monolith but never wired into a Flyway migration). It is excluded from
-- this slice; there is nothing to rename.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: tipos_identificacion -> identification_types
ALTER TABLE tipos_identificacion RENAME TO identification_types;
ALTER TABLE identification_types RENAME COLUMN id_tipo_identificacion TO id;
ALTER TABLE identification_types RENAME COLUMN nombre TO name;
ALTER TABLE identification_types RENAME COLUMN caracteres TO characters;
ALTER SEQUENCE IF EXISTS tipos_identificacion_id_tipo_identificacion_seq
    RENAME TO identification_types_id_seq;

-- Table: tipos_de_documento -> document_types
ALTER TABLE tipos_de_documento RENAME TO document_types;
ALTER TABLE document_types RENAME COLUMN id_tipo_documento TO id;
ALTER TABLE document_types RENAME COLUMN nombre TO name;
ALTER TABLE document_types RENAME COLUMN devuelto TO returned;
ALTER TABLE document_types RENAME COLUMN vence TO expires;
ALTER TABLE document_types RENAME COLUMN dias_vencimiento TO due_days;
ALTER TABLE document_types RENAME COLUMN importe_a_pagar TO amount_to_pay;
ALTER TABLE document_types RENAME COLUMN habilitado TO enabled;
ALTER TABLE document_types RENAME COLUMN quien_entrega TO delivered_by;
ALTER SEQUENCE IF EXISTS tipos_de_documento_id_tipo_documento_seq
    RENAME TO document_types_id_seq;

-- Table: tipos_de_folio -> folio_types
ALTER TABLE tipos_de_folio RENAME TO folio_types;
ALTER TABLE folio_types RENAME COLUMN id_tipo_folio TO id;
ALTER TABLE folio_types RENAME COLUMN nombre TO name;
ALTER TABLE folio_types RENAME COLUMN observaciones TO notes;
ALTER TABLE folio_types RENAME COLUMN habilitado TO enabled;
ALTER TABLE folio_types RENAME COLUMN es_auxiliar TO is_auxiliary;
ALTER SEQUENCE IF EXISTS tipos_de_folio_id_tipo_folio_seq
    RENAME TO folio_types_id_seq;

-- Table: tipos_de_tramite -> procedure_types
ALTER TABLE tipos_de_tramite RENAME TO procedure_types;
ALTER TABLE procedure_types RENAME COLUMN id_tipo_tramite TO id;
ALTER TABLE procedure_types RENAME COLUMN nombre TO name;
ALTER TABLE procedure_types RENAME COLUMN observaciones TO notes;
ALTER TABLE procedure_types RENAME COLUMN habilitado TO enabled;
ALTER TABLE procedure_types RENAME COLUMN se_archiva TO is_archived;
ALTER TABLE procedure_types RENAME COLUMN se_inscribe TO is_registered;
ALTER TABLE procedure_types RENAME COLUMN asocia_inmuebles TO associates_properties;
ALTER SEQUENCE IF EXISTS tipos_de_tramite_id_tipo_tramite_seq
    RENAME TO procedure_types_id_seq;

-- Table: roles (name already English; only its own columns are Spanish)
ALTER TABLE roles RENAME COLUMN id_rol TO id;
ALTER TABLE roles RENAME COLUMN nombre TO name;
ALTER TABLE roles RENAME COLUMN descripcion TO description;
ALTER TABLE roles RENAME COLUMN activo TO active;
ALTER SEQUENCE IF EXISTS roles_id_rol_seq RENAME TO roles_id_seq;

-- Table: roles_permisos -> role_modules (Role.java's own @ElementCollection
-- table; not a standalone JPA entity, but owned by the Role mapping updated
-- in this slice)
ALTER TABLE roles_permisos RENAME TO role_modules;
ALTER TABLE role_modules RENAME COLUMN fk_id_rol TO fk_id_role;
ALTER TABLE role_modules RENAME COLUMN modulo TO module;

-- Table: estados_de_gestion -> management_statuses
ALTER TABLE estados_de_gestion RENAME TO management_statuses;
ALTER TABLE management_statuses RENAME COLUMN id_estado_gestion TO id;
ALTER TABLE management_statuses RENAME COLUMN nombre TO name;
ALTER TABLE management_statuses RENAME COLUMN observaciones TO notes;
ALTER SEQUENCE IF EXISTS estados_de_gestion_id_estado_gestion_seq
    RENAME TO management_statuses_id_seq;

-- Table: registro_auditoria -> audit_records
ALTER TABLE registro_auditoria RENAME TO audit_records;
ALTER TABLE audit_records RENAME COLUMN id_registro_auditoria TO id;
ALTER TABLE audit_records RENAME COLUMN fecha TO date;
ALTER TABLE audit_records RENAME COLUMN modulo TO module;
ALTER TABLE audit_records RENAME COLUMN detalle_operacion TO operation_detail;
ALTER SEQUENCE IF EXISTS registro_auditoria_id_registro_auditoria_seq
    RENAME TO audit_records_id_seq;

-- Table: cuadernos -> notebooks
ALTER TABLE cuadernos RENAME TO notebooks;
ALTER TABLE notebooks RENAME COLUMN id_cuaderno TO id;
ALTER TABLE notebooks RENAME COLUMN numero TO number;
ALTER TABLE notebooks RENAME COLUMN anio TO year;
ALTER TABLE notebooks RENAME COLUMN observaciones TO notes;
ALTER TABLE notebooks RENAME COLUMN fk_id_persona_escribano TO fk_id_notary_person;
ALTER SEQUENCE IF EXISTS cuadernos_id_cuaderno_seq RENAME TO notebooks_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM identification_types;
-- SELECT COUNT(*) FROM document_types;
-- SELECT COUNT(*) FROM folio_types;
-- SELECT COUNT(*) FROM procedure_types;
-- SELECT COUNT(*) FROM roles;
-- SELECT COUNT(*) FROM role_modules;
-- SELECT COUNT(*) FROM management_statuses;
-- SELECT COUNT(*) FROM audit_records;
-- SELECT COUNT(*) FROM notebooks;
