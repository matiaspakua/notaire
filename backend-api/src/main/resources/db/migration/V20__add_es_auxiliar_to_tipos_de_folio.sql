-- =============================================================================
-- V20__add_es_auxiliar_to_tipos_de_folio.sql
-- =============================================================================
-- Description: Flags a TipoDeFolio as belonging to Protocolo Auxiliar (CU81)
-- =============================================================================

ALTER TABLE tipos_de_folio
    ADD COLUMN IF NOT EXISTS es_auxiliar boolean NOT NULL DEFAULT false;
