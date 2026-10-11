-- =============================================================================
-- V45__rename_remaining_spanish_identifiers.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-10
-- Description: Issue #1490 / CU75. Renames the Spanish table and columns that
--              the English JPA model still mapped after epic #973. Data,
--              constraints, and indexes stay attached to the renamed objects.
-- Amended:     #1499, with Owner approval (CONSTITUTION section 12 exception):
--              the identifications step failed on every fresh database. Databases
--              that already applied the original V45 need `flyway repair`.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Flyway never created "identificaciones" (see V26): it only exists on legacy databases built outside
-- Flyway. Rename it where it exists; create the English table where it does not (#1499).
DO $$
BEGIN
    IF to_regclass('public.identificaciones') IS NOT NULL THEN
        ALTER TABLE identificaciones RENAME TO identifications;
        ALTER TABLE identifications RENAME COLUMN numero TO number;
        ALTER TABLE identifications RENAME COLUMN fk_id_persona TO fk_id_person;
        ALTER TABLE identifications RENAME COLUMN fk_id_tipo_identificacion TO fk_id_identification_type;
    ELSE
        CREATE TABLE IF NOT EXISTS identifications (
            fk_id_person integer NOT NULL REFERENCES people (id),
            fk_id_identification_type integer NOT NULL REFERENCES identification_types (id),
            number integer NOT NULL,
            PRIMARY KEY (fk_id_person, fk_id_identification_type)
        );
    END IF;
END $$;

ALTER TABLE people RENAME COLUMN fecha_nacimiento TO birth_date;
ALTER TABLE people RENAME COLUMN fk_id_tipo_identificacion TO fk_id_identification_type;

ALTER TABLE workflow_definition RENAME COLUMN nombre TO name;
ALTER TABLE workflow_definition RENAME COLUMN descripcion TO description;
ALTER TABLE workflow_definition RENAME COLUMN activo TO active;

ALTER TABLE workflow_node RENAME COLUMN tipo TO node_type;
ALTER TABLE workflow_node RENAME COLUMN posicion_x TO position_x;
ALTER TABLE workflow_node RENAME COLUMN posicion_y TO position_y;
ALTER TABLE workflow_node RENAME COLUMN fk_estado_gestion_id TO fk_management_status_id;

ALTER TABLE workflow_transition RENAME COLUMN fk_nodo_origen_id TO fk_origin_node_id;
ALTER TABLE workflow_transition RENAME COLUMN fk_nodo_destino_id TO fk_destination_node_id;
ALTER TABLE workflow_transition RENAME COLUMN condicion TO condition;
ALTER TABLE workflow_transition RENAME COLUMN descripcion TO description;

ALTER TABLE procedure_folders RENAME COLUMN fk_id_gestion TO fk_id_deed_management;
ALTER TABLE procedure_folders RENAME COLUMN fk_id_tramite TO fk_id_procedure;

ALTER TABLE procedures RENAME COLUMN fk_id_presupuesto TO fk_id_budget;
ALTER TABLE procedures RENAME COLUMN fk_id_escritura TO fk_id_deed;
ALTER TABLE procedures RENAME COLUMN fk_id_gestion TO fk_id_deed_management;

ALTER TABLE submitted_documents RENAME COLUMN fk_id_tramite TO fk_id_procedure;
ALTER TABLE copies RENAME COLUMN fk_id_testimonio TO fk_id_testimony;
ALTER TABLE payments RENAME COLUMN fk_id_presupuesto TO fk_id_budget;
ALTER TABLE items RENAME COLUMN fk_id_presupuesto TO fk_id_budget;
ALTER TABLE testimony_movements RENAME COLUMN fk_id_testimonio TO fk_id_testimony;
ALTER TABLE registration_drafts RENAME COLUMN fk_id_escritura TO fk_id_deed;
ALTER TABLE audit_records RENAME COLUMN fk_id_usuario TO fk_id_user;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT table_name FROM information_schema.tables WHERE table_name = 'identifications';
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'people' AND column_name IN ('birth_date', 'fk_id_identification_type');
