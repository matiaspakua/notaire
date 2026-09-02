-- =============================================================================
-- V21__create_plantilla_costos_documento.sql
-- =============================================================================
-- Description: Creates plantilla_costos_documento, letting a TipoDeTramite's
-- presupuesto template define an expected fixed or variable cost per
-- TipoDeDocumento (Issue #823, CU27/CU39).
-- =============================================================================

CREATE TABLE IF NOT EXISTS plantilla_costos_documento (
    fk_id_tipo_tramite INTEGER NOT NULL REFERENCES tipos_de_tramite(id_tipo_tramite),
    fk_id_tipo_documento INTEGER NOT NULL REFERENCES tipos_de_documento(id_tipo_documento),
    monto_fijo NUMERIC,
    porcentaje_variable NUMERIC,
    version INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (fk_id_tipo_tramite, fk_id_tipo_documento)
);
