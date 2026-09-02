-- =============================================================================
-- V19__create_cuadernos_table.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-09-01
-- Description: CU80 - Administrar Cuadernos de Folios (issue #839). A cuaderno
--              groups exactly ten consecutive folios of the same registro
--              notarial with a correlative number per registro/año, so the
--              protocol carátula can be emitted. Folio gains an optional
--              back-reference to the cuaderno it was assigned to; this is
--              additive and does not change any existing folio behavior.
--              See openspec/changes/protocolo-cuadernos-de-folios/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS cuadernos (
    version integer NOT NULL,
    id_cuaderno SERIAL PRIMARY KEY,
    numero integer NOT NULL,
    anio integer NOT NULL,
    observaciones text,
    fk_id_persona_escribano integer NOT NULL REFERENCES personas(id_persona),
    CONSTRAINT uq_cuaderno_numero_anio_escribano UNIQUE (numero, anio, fk_id_persona_escribano)
);

ALTER TABLE folios
    ADD COLUMN IF NOT EXISTS fk_id_cuaderno integer REFERENCES cuadernos(id_cuaderno);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT table_name FROM information_schema.tables WHERE table_name = 'cuadernos';
-- Expect: 1 row.
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'folios' AND column_name = 'fk_id_cuaderno';
-- Expect: 1 row.
