-- =============================================================================
-- V3__add_missing_entity_columns.sql
-- =============================================================================
-- Author: Claude
-- Date: 2026-05-09
-- Description: Add columns present in JPA entities but absent from V1 schema.
--              These omissions cause 500 errors on GET /api/v1/items and
--              PUT /api/v1/personas/{id} (via eager Folio -> TipoDeFolio join).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- items table is missing 'observaciones' (Item.java declares @Column("observaciones"))
ALTER TABLE items ADD COLUMN IF NOT EXISTS observaciones text;

-- tipos_de_folio table is missing 'observaciones' and 'habilitado'
-- (TipoDeFolio.java declares both columns)
ALTER TABLE tipos_de_folio ADD COLUMN IF NOT EXISTS observaciones text;
ALTER TABLE tipos_de_folio ADD COLUMN IF NOT EXISTS habilitado boolean NOT NULL DEFAULT true;

-- testimonios table is missing 'observado'
-- (Testimonio.java declares @Column("observado"))
ALTER TABLE testimonios ADD COLUMN IF NOT EXISTS observado boolean NOT NULL DEFAULT false;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'items' AND column_name = 'observaciones';
-- SELECT column_name FROM information_schema.columns
-- WHERE table_name = 'tipos_de_folio' AND column_name IN ('observaciones', 'habilitado');
