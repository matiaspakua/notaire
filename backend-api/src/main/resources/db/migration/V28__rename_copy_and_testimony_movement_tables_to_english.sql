-- =============================================================================
-- V28__rename_copy_and_testimony_movement_tables_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 3 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- copias, folios_copias and movimientos_testimonio and their own columns
-- to English, matching the field names already used by the corresponding
-- (already English) JPA entities: Copy, FolioCopies, TestimonyMovement.
--
-- fk_id_testimonio (on copias and movimientos_testimonio) and fk_id_folio
-- (on folios_copias) are intentionally left untouched: they point at
-- "testimonios" and "folios", neither of which is part of this slice (see
-- tasks.md Slices 7 and 9) -- renaming them now would create an English
-- column name pointing at a still-Spanish table.
--
-- fk_id_persona on copias IS renamed here (to fk_id_person): it already
-- points at "people" (renamed in the first slice, V25), so both sides are
-- now English. Likewise fk_id_copia on folios_copias is renamed (to
-- fk_id_copy) since "copias" is renamed to "copies" in this very slice.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: copias -> copies
ALTER TABLE copias RENAME TO copies;
ALTER TABLE copies RENAME COLUMN id_copia TO id;
ALTER TABLE copies RENAME COLUMN numero TO number;
ALTER TABLE copies RENAME COLUMN fecha_impresion TO print_date;
ALTER TABLE copies RENAME COLUMN fecha_retiro TO pickup_date;
ALTER TABLE copies RENAME COLUMN observaciones TO notes;
ALTER TABLE copies RENAME COLUMN fk_id_persona TO fk_id_person;
ALTER SEQUENCE IF EXISTS copias_id_copia_seq RENAME TO copies_id_seq;

-- Table: folios_copias -> folio_copies
ALTER TABLE folios_copias RENAME TO folio_copies;
ALTER TABLE folio_copies RENAME COLUMN fk_id_copia TO fk_id_copy;

-- Table: movimientos_testimonio -> testimony_movements
ALTER TABLE movimientos_testimonio RENAME TO testimony_movements;
ALTER TABLE testimony_movements RENAME COLUMN id_movimiento_testimonio TO id;
ALTER TABLE testimony_movements RENAME COLUMN fecha_ingreso TO entry_date;
ALTER TABLE testimony_movements RENAME COLUMN fecha_salida TO exit_date;
ALTER TABLE testimony_movements RENAME COLUMN fecha_inscripcion TO registration_date;
ALTER TABLE testimony_movements RENAME COLUMN inscripta TO registered;
ALTER TABLE testimony_movements RENAME COLUMN numero_carton TO folder_number;
ALTER TABLE testimony_movements RENAME COLUMN observaciones TO notes;
ALTER SEQUENCE IF EXISTS movimientos_testimonio_id_movimiento_testimonio_seq
    RENAME TO testimony_movements_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM copies;
-- SELECT COUNT(*) FROM folio_copies;
-- SELECT COUNT(*) FROM testimony_movements;
