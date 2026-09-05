-- =============================================================================
-- V23__create_carpetas_tramite_table.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-09-04
-- Description: CU85 - Administrar Carpetas de Trámite (issue #839). A carpeta
--              de trámite groups the documentation of a single trámite within
--              a gestión, with an activa/espera/archivada lifecycle. One
--              carpeta is generated automatically per trámite. See
--              openspec/changes/protocolo-carpetas-de-tramite/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS carpetas_tramite (
    id_carpeta SERIAL PRIMARY KEY,
    numero integer NOT NULL,
    estado varchar(20) NOT NULL,
    motivo_espera text,
    fk_id_gestion integer NOT NULL REFERENCES gestiones_de_escrituras(id_gestion),
    fk_id_tramite integer NOT NULL UNIQUE REFERENCES tramites(id_tramite),
    CONSTRAINT uq_carpeta_tramite_numero UNIQUE (numero)
);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT table_name FROM information_schema.tables WHERE table_name = 'carpetas_tramite';
-- Expect: 1 row.
