-- =============================================================================
-- V16__add_metodo_pago_to_pagos.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-08-20
-- Description: CU15 - Procesar pago collects a metodoPago on the frontend but
--              the backend has no column to persist it. Adds an additive,
--              nullable free-text column so existing rows are unaffected. See
--              openspec/changes/persist-metodo-pago-on-pago/design.md.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE pagos
    ADD COLUMN IF NOT EXISTS metodo_pago TEXT;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name, is_nullable, data_type FROM information_schema.columns
--   WHERE table_name = 'pagos' AND column_name = 'metodo_pago';
-- Expect: 1 row, is_nullable = 'YES', data_type = 'text'.
