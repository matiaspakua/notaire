-- =============================================================================
-- V21__add_tipo_motivo_to_items.sql
-- =============================================================================
-- Description: Adds tipo (normal/descuento/recargo) and motivo columns to
--              items so a presupuesto item can be classified as a discount
--              or surcharge with a mandatory reason (CU45, CU71).
-- =============================================================================

ALTER TABLE items
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE items
    ADD COLUMN IF NOT EXISTS motivo VARCHAR(255);
