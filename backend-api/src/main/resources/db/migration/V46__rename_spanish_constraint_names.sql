-- =============================================================================
-- V46__rename_spanish_constraint_names.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-12
-- Description: Issue #1490 / CU75. Renames constraint names that PostgreSQL
--              kept when earlier migrations renamed the tables and columns.
--              The constraints stay attached to the same columns. Row values
--              are unchanged.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'registro_auditoria_fk_id_usuario_fkey') THEN
        ALTER TABLE audit_records RENAME CONSTRAINT registro_auditoria_fk_id_usuario_fkey TO audit_records_fk_id_user_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'presupuestos_fk_id_persona_fkey') THEN
        ALTER TABLE budgets RENAME CONSTRAINT presupuestos_fk_id_persona_fkey TO budgets_fk_id_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'presupuestos_pkey') THEN
        ALTER TABLE budgets RENAME CONSTRAINT presupuestos_pkey TO budgets_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_presupuestos_fk_id_concepto_fkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT plantilla_presupuestos_fk_id_concepto_fkey TO budget_templates_fk_id_concept_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_presupuestos_fk_id_tipo_tramite_fkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT plantilla_presupuestos_fk_id_tipo_tramite_fkey TO budget_templates_fk_id_procedure_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_presupuestos_pkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT plantilla_presupuestos_pkey TO budget_templates_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copias_fk_id_persona_fkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copias_fk_id_persona_fkey TO copies_fk_id_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copias_fk_id_testimonio_fkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copias_fk_id_testimonio_fkey TO copies_fk_id_testimony_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copias_pkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copias_pkey TO copies_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'gestiones_de_escrituras_fk_id_estado_de_gestion_fkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT gestiones_de_escrituras_fk_id_estado_de_gestion_fkey TO deed_managements_fk_id_management_status_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'gestiones_de_escrituras_fk_id_persona_escribano_fkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT gestiones_de_escrituras_fk_id_persona_escribano_fkey TO deed_managements_fk_id_notary_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'gestiones_de_escrituras_pkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT gestiones_de_escrituras_pkey TO deed_managements_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'escrituras_pkey') THEN
        ALTER TABLE deeds RENAME CONSTRAINT escrituras_pkey TO deeds_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_costos_documento_fk_id_tipo_documento_fkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT plantilla_costos_documento_fk_id_tipo_documento_fkey TO document_cost_templates_fk_id_document_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_costos_documento_fk_id_tipo_tramite_fkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT plantilla_costos_documento_fk_id_tipo_tramite_fkey TO document_cost_templates_fk_id_procedure_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_costos_documento_pkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT plantilla_costos_documento_pkey TO document_cost_templates_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tipos_de_documento_pkey') THEN
        ALTER TABLE document_types RENAME CONSTRAINT tipos_de_documento_pkey TO document_types_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_copias_fk_id_copia_fkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folios_copias_fk_id_copia_fkey TO folio_copies_fk_id_copy_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_copias_fk_id_folio_fkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folios_copias_fk_id_folio_fkey TO folio_copies_fk_id_folio_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_copias_pkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folios_copias_pkey TO folio_copies_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_cuaderno_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_cuaderno_fkey TO folios_fk_id_notebook_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_escritura_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_escritura_fkey TO folios_fk_id_deed_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_persona_escribano_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_persona_escribano_fkey TO folios_fk_id_notary_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tipos_de_folio_pkey') THEN
        ALTER TABLE folio_types RENAME CONSTRAINT tipos_de_folio_pkey TO folio_types_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'historial_fk_id_estado_gestion_fkey') THEN
        ALTER TABLE history RENAME CONSTRAINT historial_fk_id_estado_gestion_fkey TO history_fk_id_management_status_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'historial_fk_id_gestion_fkey') THEN
        ALTER TABLE history RENAME CONSTRAINT historial_fk_id_gestion_fkey TO history_fk_id_deed_management_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'historial_pkey') THEN
        ALTER TABLE history RENAME CONSTRAINT historial_pkey TO history_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tipos_identificacion_pkey') THEN
        ALTER TABLE identification_types RENAME CONSTRAINT tipos_identificacion_pkey TO identification_types_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'items_fk_id_presupuesto_fkey') THEN
        ALTER TABLE items RENAME CONSTRAINT items_fk_id_presupuesto_fkey TO items_fk_id_budget_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'estados_de_gestion_pkey') THEN
        ALTER TABLE management_statuses RENAME CONSTRAINT estados_de_gestion_pkey TO management_statuses_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'cuadernos_fk_id_persona_escribano_fkey') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT cuadernos_fk_id_persona_escribano_fkey TO notebooks_fk_id_notary_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'cuadernos_pkey') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT cuadernos_pkey TO notebooks_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_cuaderno_numero_anio_escribano') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT uq_cuaderno_numero_anio_escribano TO notebooks_number_year_number_fk_id_notary_person_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pagos_fk_id_presupuesto_fkey') THEN
        ALTER TABLE payments RENAME CONSTRAINT pagos_fk_id_presupuesto_fkey TO payments_fk_id_budget_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'pagos_pkey') THEN
        ALTER TABLE payments RENAME CONSTRAINT pagos_pkey TO payments_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'personas_fk_id_tipo_identificacion_fkey') THEN
        ALTER TABLE people RENAME CONSTRAINT personas_fk_id_tipo_identificacion_fkey TO people_fk_id_identification_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'personas_pkey') THEN
        ALTER TABLE people RENAME CONSTRAINT personas_pkey TO people_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_personas_fk_id_persona_cliente_fkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT tramites_personas_fk_id_persona_cliente_fkey TO person_procedures_fk_id_client_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_personas_fk_id_tramite_fkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT tramites_personas_fk_id_tramite_fkey TO person_procedures_fk_id_procedure_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_personas_pkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT tramites_personas_pkey TO person_procedures_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'carpetas_tramite_fk_id_gestion_fkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT carpetas_tramite_fk_id_gestion_fkey TO procedure_folders_fk_id_deed_management_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'carpetas_tramite_fk_id_tramite_fkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT carpetas_tramite_fk_id_tramite_fkey TO procedure_folders_fk_id_procedure_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'carpetas_tramite_fk_id_tramite_key') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT carpetas_tramite_fk_id_tramite_key TO procedure_folders_fk_id_procedure_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'carpetas_tramite_pkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT carpetas_tramite_pkey TO procedure_folders_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_carpeta_tramite_numero') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT uq_carpeta_tramite_numero TO procedure_folders_number_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_tramites_presupuesto') THEN
        ALTER TABLE procedures RENAME CONSTRAINT fk_tramites_presupuesto TO procedures_fk_id_budget_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_fk_id_escritura_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT tramites_fk_id_escritura_fkey TO procedures_fk_id_deed_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_fk_id_gestion_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT tramites_fk_id_gestion_fkey TO procedures_fk_id_deed_management_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_fk_id_inmueble_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT tramites_fk_id_inmueble_fkey TO procedures_fk_id_property_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_fk_id_tipo_tramite_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT tramites_fk_id_tipo_tramite_fkey TO procedures_fk_id_procedure_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tramites_pkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT tramites_pkey TO procedures_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_tramites_fk_id_tipo_documento_fkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT plantilla_tramites_fk_id_tipo_documento_fkey TO procedure_templates_fk_id_document_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_tramites_fk_id_tipo_tramite_fkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT plantilla_tramites_fk_id_tipo_tramite_fkey TO procedure_templates_fk_id_procedure_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'plantilla_tramites_pkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT plantilla_tramites_pkey TO procedure_templates_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tipos_de_tramite_fk_workflow_definition_id_fkey') THEN
        ALTER TABLE procedure_types RENAME CONSTRAINT tipos_de_tramite_fk_workflow_definition_id_fkey TO procedure_types_fk_workflow_definition_id_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'tipos_de_tramite_pkey') THEN
        ALTER TABLE procedure_types RENAME CONSTRAINT tipos_de_tramite_pkey TO procedure_types_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'inmuebles_pkey') THEN
        ALTER TABLE properties RENAME CONSTRAINT inmuebles_pkey TO properties_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'minutas_inscripcion_fk_id_escritura_fkey') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT minutas_inscripcion_fk_id_escritura_fkey TO registration_drafts_fk_id_deed_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'minutas_inscripcion_fk_id_escritura_key') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT minutas_inscripcion_fk_id_escritura_key TO registration_drafts_fk_id_deed_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'minutas_inscripcion_pkey') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT minutas_inscripcion_pkey TO registration_drafts_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'roles_nombre_unique') THEN
        ALTER TABLE roles RENAME CONSTRAINT roles_nombre_unique TO roles_name_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'documentos_presentados_fk_id_tipo_documento_fkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT documentos_presentados_fk_id_tipo_documento_fkey TO submitted_documents_fk_id_document_type_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'documentos_presentados_fk_id_tramite_fkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT documentos_presentados_fk_id_tramite_fkey TO submitted_documents_fk_id_procedure_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'documentos_presentados_pkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT documentos_presentados_pkey TO submitted_documents_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'suplencias_fk_id_suplantado_fkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT suplencias_fk_id_suplantado_fkey TO substitutions_fk_id_substituted_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'suplencias_fk_id_suplente_fkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT suplencias_fk_id_suplente_fkey TO substitutions_fk_id_substitute_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'suplencias_pkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT suplencias_pkey TO substitutions_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimonios_fk_id_escritura_fkey') THEN
        ALTER TABLE testimonies RENAME CONSTRAINT testimonios_fk_id_escritura_fkey TO testimonies_fk_id_deed_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimonios_pkey') THEN
        ALTER TABLE testimonies RENAME CONSTRAINT testimonios_pkey TO testimonies_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'movimientos_testimonio_fk_id_testimonio_fkey') THEN
        ALTER TABLE testimony_movements RENAME CONSTRAINT movimientos_testimonio_fk_id_testimonio_fkey TO testimony_movements_fk_id_testimony_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'movimientos_testimonio_pkey') THEN
        ALTER TABLE testimony_movements RENAME CONSTRAINT movimientos_testimonio_pkey TO testimony_movements_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'usuarios_fk_id_persona_fkey') THEN
        ALTER TABLE users RENAME CONSTRAINT usuarios_fk_id_persona_fkey TO users_fk_id_person_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'usuarios_fk_id_rol_fkey') THEN
        ALTER TABLE users RENAME CONSTRAINT usuarios_fk_id_rol_fkey TO users_fk_id_role_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'usuarios_pkey') THEN
        ALTER TABLE users RENAME CONSTRAINT usuarios_pkey TO users_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'workflow_node_fk_estado_gestion_id_fkey') THEN
        ALTER TABLE workflow_node RENAME CONSTRAINT workflow_node_fk_estado_gestion_id_fkey TO workflow_node_fk_management_status_id_fkey;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT conname FROM pg_constraint
--  WHERE connamespace = 'public'::regnamespace
--    AND conname ~ '(gestion|tramite|usuario|presupuesto|inmueble|escritura)';
