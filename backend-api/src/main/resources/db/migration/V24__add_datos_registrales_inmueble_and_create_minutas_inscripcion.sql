-- =============================================================================
-- V23__add_datos_registrales_inmueble_and_create_minutas_inscripcion.sql
-- =============================================================================
-- Description: Adds matrícula, tomo/folio/finca and linderos to inmuebles, and
-- creates minutas_inscripcion to track the registry submission circuit
-- (Generada -> Presentada -> Observada / Inscripta) for an escritura sobre un
-- inmueble (Issue #839, CU82).
-- =============================================================================

ALTER TABLE inmuebles
    ADD COLUMN IF NOT EXISTS matricula VARCHAR(255),
    ADD COLUMN IF NOT EXISTS tomo_folio_finca VARCHAR(255),
    ADD COLUMN IF NOT EXISTS linderos TEXT;

CREATE TABLE IF NOT EXISTS minutas_inscripcion (
    id_minuta_inscripcion SERIAL PRIMARY KEY,
    numero INTEGER NOT NULL,
    precio_operacion NUMERIC,
    estado VARCHAR(50) NOT NULL,
    fecha_generacion DATE NOT NULL,
    fecha_presentacion DATE,
    numero_entrada_registral VARCHAR(255),
    fecha_recepcion DATE,
    numero_inscripcion_definitivo VARCHAR(255),
    observaciones_registro TEXT,
    fecha_subsanacion DATE,
    fk_id_escritura INTEGER NOT NULL UNIQUE REFERENCES escrituras(id_escritura),
    version INTEGER NOT NULL DEFAULT 0
);
