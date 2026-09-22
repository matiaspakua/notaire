-- =============================================================================
-- V29__rename_document_and_template_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 4 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- documentos_presentados, plantilla_tramites, plantilla_presupuestos and
-- plantilla_costos_documento and their own columns to English, matching
-- the field names already used by the corresponding (already English)
-- JPA entities: SubmittedDocument, ProcedureTemplate, BudgetTemplate,
-- DocumentCostTemplate.
--
-- fk_id_tramite on documentos_presentados is intentionally left untouched:
-- it points at "tramites", not yet renamed (Slice 6).
--
-- fk_id_tipo_tramite, fk_id_tipo_documento and fk_id_concepto ARE renamed
-- here (to fk_id_procedure_type, fk_id_document_type, fk_id_concept) even
-- though they belong to owning tables renamed in earlier slices too --
-- their target tables (procedure_types, document_types, concepts) were
-- already renamed in Slices 1-2, so both sides are now consistently
-- English.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: documentos_presentados -> submitted_documents
ALTER TABLE documentos_presentados RENAME TO submitted_documents;
ALTER TABLE submitted_documents RENAME COLUMN id_documento_presentado TO id;
ALTER TABLE submitted_documents RENAME COLUMN nombre TO name;
ALTER TABLE submitted_documents RENAME COLUMN numero_carton TO folder_number;
ALTER TABLE submitted_documents RENAME COLUMN fecha_ingreso TO entry_date;
ALTER TABLE submitted_documents RENAME COLUMN fecha_salida TO exit_date;
ALTER TABLE submitted_documents RENAME COLUMN preparado TO prepared;
ALTER TABLE submitted_documents RENAME COLUMN vence TO expires;
ALTER TABLE submitted_documents RENAME COLUMN fecha_vencimiento TO due_date;
ALTER TABLE submitted_documents RENAME COLUMN dias_vencimiento TO due_days;
ALTER TABLE submitted_documents RENAME COLUMN importe_a_pagar TO amount_to_pay;
ALTER TABLE submitted_documents RENAME COLUMN fecha_pago TO payment_date;
ALTER TABLE submitted_documents RENAME COLUMN liberado TO released;
ALTER TABLE submitted_documents RENAME COLUMN fecha_liberado TO released_date;
ALTER TABLE submitted_documents RENAME COLUMN observado TO observed;
ALTER TABLE submitted_documents RENAME COLUMN observaciones TO notes;
ALTER TABLE submitted_documents RENAME COLUMN entregado TO delivered;
ALTER TABLE submitted_documents RENAME COLUMN reingresado TO reentered;
ALTER TABLE submitted_documents RENAME COLUMN quien_entrega TO delivered_by;
ALTER TABLE submitted_documents RENAME COLUMN fk_id_tipo_documento TO fk_id_document_type;
ALTER SEQUENCE IF EXISTS documentos_presentados_id_documento_presentado_seq
    RENAME TO submitted_documents_id_seq;

-- Table: plantilla_tramites -> procedure_templates
ALTER TABLE plantilla_tramites RENAME TO procedure_templates;
ALTER TABLE procedure_templates RENAME COLUMN fk_id_tipo_tramite TO fk_id_procedure_type;
ALTER TABLE procedure_templates RENAME COLUMN fk_id_tipo_documento TO fk_id_document_type;
ALTER TABLE procedure_templates RENAME COLUMN observaciones TO notes;

-- Table: plantilla_presupuestos -> budget_templates
ALTER TABLE plantilla_presupuestos RENAME TO budget_templates;
ALTER TABLE budget_templates RENAME COLUMN fk_id_tipo_tramite TO fk_id_procedure_type;
ALTER TABLE budget_templates RENAME COLUMN fk_id_concepto TO fk_id_concept;
ALTER TABLE budget_templates RENAME COLUMN observaciones TO notes;

-- Table: plantilla_costos_documento -> document_cost_templates
ALTER TABLE plantilla_costos_documento RENAME TO document_cost_templates;
ALTER TABLE document_cost_templates RENAME COLUMN fk_id_tipo_tramite TO fk_id_procedure_type;
ALTER TABLE document_cost_templates RENAME COLUMN fk_id_tipo_documento TO fk_id_document_type;
ALTER TABLE document_cost_templates RENAME COLUMN monto_fijo TO fixed_amount;
ALTER TABLE document_cost_templates RENAME COLUMN porcentaje_variable TO variable_percentage;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM submitted_documents;
-- SELECT COUNT(*) FROM procedure_templates;
-- SELECT COUNT(*) FROM budget_templates;
-- SELECT COUNT(*) FROM document_cost_templates;
