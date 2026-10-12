-- =============================================================================
-- R46__rename_spanish_constraint_names.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-12
-- Description: Reverse of V46. Restores the previous constraint names.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration (Rollback)
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'audit_records_fk_id_user_fkey') THEN
        ALTER TABLE audit_records RENAME CONSTRAINT audit_records_fk_id_user_fkey TO registro_auditoria_fk_id_usuario_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'budgets_fk_id_person_fkey') THEN
        ALTER TABLE budgets RENAME CONSTRAINT budgets_fk_id_person_fkey TO presupuestos_fk_id_persona_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'budgets_pkey') THEN
        ALTER TABLE budgets RENAME CONSTRAINT budgets_pkey TO presupuestos_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'budget_templates_fk_id_concept_fkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT budget_templates_fk_id_concept_fkey TO plantilla_presupuestos_fk_id_concepto_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'budget_templates_fk_id_procedure_type_fkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT budget_templates_fk_id_procedure_type_fkey TO plantilla_presupuestos_fk_id_tipo_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'budget_templates_pkey') THEN
        ALTER TABLE budget_templates RENAME CONSTRAINT budget_templates_pkey TO plantilla_presupuestos_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copies_fk_id_person_fkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copies_fk_id_person_fkey TO copias_fk_id_persona_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copies_fk_id_testimony_fkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copies_fk_id_testimony_fkey TO copias_fk_id_testimonio_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'copies_pkey') THEN
        ALTER TABLE copies RENAME CONSTRAINT copies_pkey TO copias_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'deed_managements_fk_id_management_status_fkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT deed_managements_fk_id_management_status_fkey TO gestiones_de_escrituras_fk_id_estado_de_gestion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'deed_managements_fk_id_notary_person_fkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT deed_managements_fk_id_notary_person_fkey TO gestiones_de_escrituras_fk_id_persona_escribano_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'deed_managements_pkey') THEN
        ALTER TABLE deed_managements RENAME CONSTRAINT deed_managements_pkey TO gestiones_de_escrituras_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'deeds_pkey') THEN
        ALTER TABLE deeds RENAME CONSTRAINT deeds_pkey TO escrituras_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'document_cost_templates_fk_id_document_type_fkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT document_cost_templates_fk_id_document_type_fkey TO plantilla_costos_documento_fk_id_tipo_documento_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'document_cost_templates_fk_id_procedure_type_fkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT document_cost_templates_fk_id_procedure_type_fkey TO plantilla_costos_documento_fk_id_tipo_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'document_cost_templates_pkey') THEN
        ALTER TABLE document_cost_templates RENAME CONSTRAINT document_cost_templates_pkey TO plantilla_costos_documento_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'document_types_pkey') THEN
        ALTER TABLE document_types RENAME CONSTRAINT document_types_pkey TO tipos_de_documento_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folio_copies_fk_id_copy_fkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folio_copies_fk_id_copy_fkey TO folios_copias_fk_id_copia_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folio_copies_fk_id_folio_fkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folio_copies_fk_id_folio_fkey TO folios_copias_fk_id_folio_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folio_copies_pkey') THEN
        ALTER TABLE folio_copies RENAME CONSTRAINT folio_copies_pkey TO folios_copias_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_notebook_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_notebook_fkey TO folios_fk_id_cuaderno_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_deed_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_deed_fkey TO folios_fk_id_escritura_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folios_fk_id_notary_person_fkey') THEN
        ALTER TABLE folios RENAME CONSTRAINT folios_fk_id_notary_person_fkey TO folios_fk_id_persona_escribano_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'folio_types_pkey') THEN
        ALTER TABLE folio_types RENAME CONSTRAINT folio_types_pkey TO tipos_de_folio_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'history_fk_id_management_status_fkey') THEN
        ALTER TABLE history RENAME CONSTRAINT history_fk_id_management_status_fkey TO historial_fk_id_estado_gestion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'history_fk_id_deed_management_fkey') THEN
        ALTER TABLE history RENAME CONSTRAINT history_fk_id_deed_management_fkey TO historial_fk_id_gestion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'history_pkey') THEN
        ALTER TABLE history RENAME CONSTRAINT history_pkey TO historial_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'identification_types_pkey') THEN
        ALTER TABLE identification_types RENAME CONSTRAINT identification_types_pkey TO tipos_identificacion_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'items_fk_id_budget_fkey') THEN
        ALTER TABLE items RENAME CONSTRAINT items_fk_id_budget_fkey TO items_fk_id_presupuesto_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'management_statuses_pkey') THEN
        ALTER TABLE management_statuses RENAME CONSTRAINT management_statuses_pkey TO estados_de_gestion_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'notebooks_fk_id_notary_person_fkey') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT notebooks_fk_id_notary_person_fkey TO cuadernos_fk_id_persona_escribano_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'notebooks_pkey') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT notebooks_pkey TO cuadernos_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'notebooks_number_year_number_fk_id_notary_person_key') THEN
        ALTER TABLE notebooks RENAME CONSTRAINT notebooks_number_year_number_fk_id_notary_person_key TO uq_cuaderno_numero_anio_escribano;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'payments_fk_id_budget_fkey') THEN
        ALTER TABLE payments RENAME CONSTRAINT payments_fk_id_budget_fkey TO pagos_fk_id_presupuesto_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'payments_pkey') THEN
        ALTER TABLE payments RENAME CONSTRAINT payments_pkey TO pagos_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'people_fk_id_identification_type_fkey') THEN
        ALTER TABLE people RENAME CONSTRAINT people_fk_id_identification_type_fkey TO personas_fk_id_tipo_identificacion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'people_pkey') THEN
        ALTER TABLE people RENAME CONSTRAINT people_pkey TO personas_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'person_procedures_fk_id_client_person_fkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT person_procedures_fk_id_client_person_fkey TO tramites_personas_fk_id_persona_cliente_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'person_procedures_fk_id_procedure_fkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT person_procedures_fk_id_procedure_fkey TO tramites_personas_fk_id_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'person_procedures_pkey') THEN
        ALTER TABLE person_procedures RENAME CONSTRAINT person_procedures_pkey TO tramites_personas_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_folders_fk_id_deed_management_fkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT procedure_folders_fk_id_deed_management_fkey TO carpetas_tramite_fk_id_gestion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_folders_fk_id_procedure_fkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT procedure_folders_fk_id_procedure_fkey TO carpetas_tramite_fk_id_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_folders_fk_id_procedure_key') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT procedure_folders_fk_id_procedure_key TO carpetas_tramite_fk_id_tramite_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_folders_pkey') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT procedure_folders_pkey TO carpetas_tramite_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_folders_number_key') THEN
        ALTER TABLE procedure_folders RENAME CONSTRAINT procedure_folders_number_key TO uq_carpeta_tramite_numero;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_fk_id_budget_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_fk_id_budget_fkey TO fk_tramites_presupuesto;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_fk_id_deed_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_fk_id_deed_fkey TO tramites_fk_id_escritura_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_fk_id_deed_management_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_fk_id_deed_management_fkey TO tramites_fk_id_gestion_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_fk_id_property_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_fk_id_property_fkey TO tramites_fk_id_inmueble_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_fk_id_procedure_type_fkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_fk_id_procedure_type_fkey TO tramites_fk_id_tipo_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedures_pkey') THEN
        ALTER TABLE procedures RENAME CONSTRAINT procedures_pkey TO tramites_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_templates_fk_id_document_type_fkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT procedure_templates_fk_id_document_type_fkey TO plantilla_tramites_fk_id_tipo_documento_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_templates_fk_id_procedure_type_fkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT procedure_templates_fk_id_procedure_type_fkey TO plantilla_tramites_fk_id_tipo_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_templates_pkey') THEN
        ALTER TABLE procedure_templates RENAME CONSTRAINT procedure_templates_pkey TO plantilla_tramites_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_types_fk_workflow_definition_id_fkey') THEN
        ALTER TABLE procedure_types RENAME CONSTRAINT procedure_types_fk_workflow_definition_id_fkey TO tipos_de_tramite_fk_workflow_definition_id_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'procedure_types_pkey') THEN
        ALTER TABLE procedure_types RENAME CONSTRAINT procedure_types_pkey TO tipos_de_tramite_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'properties_pkey') THEN
        ALTER TABLE properties RENAME CONSTRAINT properties_pkey TO inmuebles_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'registration_drafts_fk_id_deed_fkey') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT registration_drafts_fk_id_deed_fkey TO minutas_inscripcion_fk_id_escritura_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'registration_drafts_fk_id_deed_key') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT registration_drafts_fk_id_deed_key TO minutas_inscripcion_fk_id_escritura_key;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'registration_drafts_pkey') THEN
        ALTER TABLE registration_drafts RENAME CONSTRAINT registration_drafts_pkey TO minutas_inscripcion_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'roles_name_key') THEN
        ALTER TABLE roles RENAME CONSTRAINT roles_name_key TO roles_nombre_unique;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'submitted_documents_fk_id_document_type_fkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT submitted_documents_fk_id_document_type_fkey TO documentos_presentados_fk_id_tipo_documento_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'submitted_documents_fk_id_procedure_fkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT submitted_documents_fk_id_procedure_fkey TO documentos_presentados_fk_id_tramite_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'submitted_documents_pkey') THEN
        ALTER TABLE submitted_documents RENAME CONSTRAINT submitted_documents_pkey TO documentos_presentados_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'substitutions_fk_id_substituted_person_fkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT substitutions_fk_id_substituted_person_fkey TO suplencias_fk_id_suplantado_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'substitutions_fk_id_substitute_person_fkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT substitutions_fk_id_substitute_person_fkey TO suplencias_fk_id_suplente_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'substitutions_pkey') THEN
        ALTER TABLE substitutions RENAME CONSTRAINT substitutions_pkey TO suplencias_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimonies_fk_id_deed_fkey') THEN
        ALTER TABLE testimonies RENAME CONSTRAINT testimonies_fk_id_deed_fkey TO testimonios_fk_id_escritura_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimonies_pkey') THEN
        ALTER TABLE testimonies RENAME CONSTRAINT testimonies_pkey TO testimonios_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimony_movements_fk_id_testimony_fkey') THEN
        ALTER TABLE testimony_movements RENAME CONSTRAINT testimony_movements_fk_id_testimony_fkey TO movimientos_testimonio_fk_id_testimonio_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'testimony_movements_pkey') THEN
        ALTER TABLE testimony_movements RENAME CONSTRAINT testimony_movements_pkey TO movimientos_testimonio_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_fk_id_person_fkey') THEN
        ALTER TABLE users RENAME CONSTRAINT users_fk_id_person_fkey TO usuarios_fk_id_persona_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_fk_id_role_fkey') THEN
        ALTER TABLE users RENAME CONSTRAINT users_fk_id_role_fkey TO usuarios_fk_id_rol_fkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_pkey') THEN
        ALTER TABLE users RENAME CONSTRAINT users_pkey TO usuarios_pkey;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'workflow_node_fk_management_status_id_fkey') THEN
        ALTER TABLE workflow_node RENAME CONSTRAINT workflow_node_fk_management_status_id_fkey TO workflow_node_fk_estado_gestion_id_fkey;
    END IF;
END $$;
