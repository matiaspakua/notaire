-- =============================================================================
-- R45__rename_remaining_spanish_identifiers.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-10
-- Description: Reverse of V45. Restores the previous identifier names. Row
--              values are unchanged by either direction.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration (Rollback)
-- -----------------------------------------------------------------------------

ALTER TABLE audit_records RENAME COLUMN fk_id_user TO fk_id_usuario;
ALTER TABLE registration_drafts RENAME COLUMN fk_id_deed TO fk_id_escritura;
ALTER TABLE testimony_movements RENAME COLUMN fk_id_testimony TO fk_id_testimonio;
ALTER TABLE items RENAME COLUMN fk_id_budget TO fk_id_presupuesto;
ALTER TABLE payments RENAME COLUMN fk_id_budget TO fk_id_presupuesto;
ALTER TABLE copies RENAME COLUMN fk_id_testimony TO fk_id_testimonio;
ALTER TABLE submitted_documents RENAME COLUMN fk_id_procedure TO fk_id_tramite;

ALTER TABLE procedures RENAME COLUMN fk_id_deed_management TO fk_id_gestion;
ALTER TABLE procedures RENAME COLUMN fk_id_deed TO fk_id_escritura;
ALTER TABLE procedures RENAME COLUMN fk_id_budget TO fk_id_presupuesto;

ALTER TABLE procedure_folders RENAME COLUMN fk_id_procedure TO fk_id_tramite;
ALTER TABLE procedure_folders RENAME COLUMN fk_id_deed_management TO fk_id_gestion;

ALTER TABLE workflow_transition RENAME COLUMN description TO descripcion;
ALTER TABLE workflow_transition RENAME COLUMN condition TO condicion;
ALTER TABLE workflow_transition RENAME COLUMN fk_destination_node_id TO fk_nodo_destino_id;
ALTER TABLE workflow_transition RENAME COLUMN fk_origin_node_id TO fk_nodo_origen_id;

ALTER TABLE workflow_node RENAME COLUMN fk_management_status_id TO fk_estado_gestion_id;
ALTER TABLE workflow_node RENAME COLUMN position_y TO posicion_y;
ALTER TABLE workflow_node RENAME COLUMN position_x TO posicion_x;
ALTER TABLE workflow_node RENAME COLUMN node_type TO tipo;

ALTER TABLE workflow_definition RENAME COLUMN active TO activo;
ALTER TABLE workflow_definition RENAME COLUMN description TO descripcion;
ALTER TABLE workflow_definition RENAME COLUMN name TO nombre;

ALTER TABLE people RENAME COLUMN fk_id_identification_type TO fk_id_tipo_identificacion;
ALTER TABLE people RENAME COLUMN birth_date TO fecha_nacimiento;

ALTER TABLE identifications RENAME COLUMN fk_id_identification_type TO fk_id_tipo_identificacion;
ALTER TABLE identifications RENAME COLUMN fk_id_person TO fk_id_persona;
ALTER TABLE identifications RENAME COLUMN number TO numero;
ALTER TABLE identifications RENAME TO identificaciones;
