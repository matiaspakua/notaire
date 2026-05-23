-- =============================================================================
-- V5__fix_movimientos_testimonio_schema.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-05-23
-- Description: Align movimientos_testimonio table with MovimientoTestimonio entity.
--   The entity expects id_movimiento_testimonio, fecha_ingreso, fecha_salida,
--   fecha_inscripcion, inscripta, and numero_carton columns; the table was
--   created with id_movimiento and fecha_movimiento instead.
-- =============================================================================

-- Rename PK column to match entity @Column(name = "id_movimiento_testimonio")
ALTER TABLE movimientos_testimonio
    RENAME COLUMN id_movimiento TO id_movimiento_testimonio;

-- Rename sequence to match (for IDENTITY generation)
ALTER SEQUENCE IF EXISTS movimientos_testimonio_id_movimiento_seq
    RENAME TO movimientos_testimonio_id_movimiento_testimonio_seq;

-- Rename fecha_movimiento -> fecha_ingreso (entry date)
ALTER TABLE movimientos_testimonio
    RENAME COLUMN fecha_movimiento TO fecha_ingreso;

-- Add missing columns required by entity
ALTER TABLE movimientos_testimonio
    ADD COLUMN IF NOT EXISTS fecha_salida date,
    ADD COLUMN IF NOT EXISTS fecha_inscripcion date,
    ADD COLUMN IF NOT EXISTS inscripta boolean NOT NULL DEFAULT false,
    ADD COLUMN IF NOT EXISTS numero_carton integer NOT NULL DEFAULT 0;
