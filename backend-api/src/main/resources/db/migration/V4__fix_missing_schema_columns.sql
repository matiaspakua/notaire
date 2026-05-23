-- =============================================================================
-- V4__fix_missing_schema_columns.sql
-- =============================================================================
-- Author: Claude
-- Date: 2026-05-23
-- Description: Add columns that V3 intended to add but were not applied because
--              Flyway did not run against the Docker-initialized database.
--              These missing columns cause 500 errors on:
--                - GET/PUT /api/v1/personas (tipos_de_folio.habilitado via Folio join)
--                - GET /api/v1/items (items.observaciones)
--                - GET /api/v1/suplencia (testimonios.observado)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- items table — missing observaciones (Item.java @Column("observaciones"))
-- -----------------------------------------------------------------------------
ALTER TABLE items ADD COLUMN IF NOT EXISTS observaciones text;

-- -----------------------------------------------------------------------------
-- tipos_de_folio table — missing observaciones and habilitado
-- (TipoDeFolio.java declares both columns; Persona → Folio → TipoDeFolio chain)
-- -----------------------------------------------------------------------------
ALTER TABLE tipos_de_folio ADD COLUMN IF NOT EXISTS observaciones text;
ALTER TABLE tipos_de_folio ADD COLUMN IF NOT EXISTS habilitado boolean NOT NULL DEFAULT true;

-- -----------------------------------------------------------------------------
-- testimonios table — missing observado (Testimonio.java @Column("observado"))
-- -----------------------------------------------------------------------------
ALTER TABLE testimonios ADD COLUMN IF NOT EXISTS observado boolean NOT NULL DEFAULT false;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'items' AND column_name = 'observaciones';
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'tipos_de_folio' AND column_name IN ('observaciones', 'habilitado');
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'testimonios' AND column_name = 'observado';
