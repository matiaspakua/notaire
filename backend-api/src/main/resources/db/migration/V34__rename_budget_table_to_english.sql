-- =============================================================================
-- V34__rename_budget_table_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Slice 8 of the remaining domain-schema-to-English rename
-- (#973, openspec/changes/translate-domain-schema-to-english). Renames
-- presupuestos and its own columns to English, matching the field names
-- already used by the corresponding (already English) JPA entity: Budget.
--
-- presupuestos.fk_id_tramite was already dropped in V14 (circular FK
-- cleanup) -- nothing to rename there. fk_id_persona IS renamed here (to
-- fk_id_person): it already points at "people" (renamed in V25), so both
-- sides are now consistently English.
--
-- "fecha" is renamed to "budget_date", not the literal "date": DATE is an
-- H2 reserved keyword (same class of issue as value/type in V27 and year
-- in V32/V33), and this project's H2 unit-test profile builds its schema
-- from these same JPA annotations (ddl-auto=create).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: presupuestos -> budgets
ALTER TABLE presupuestos RENAME TO budgets;
ALTER TABLE budgets RENAME COLUMN id_presupuesto TO id;
ALTER TABLE budgets RENAME COLUMN numero TO number;
ALTER TABLE budgets RENAME COLUMN fecha TO budget_date;
ALTER TABLE budgets RENAME COLUMN encabezado TO heading;
ALTER TABLE budgets RENAME COLUMN observaciones TO notes;
ALTER TABLE budgets RENAME COLUMN estado TO status;
ALTER TABLE budgets RENAME COLUMN monto_inmueble TO property_amount;
ALTER TABLE budgets RENAME COLUMN fk_id_persona TO fk_id_person;
ALTER SEQUENCE IF EXISTS presupuestos_id_presupuesto_seq RENAME TO budgets_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM budgets;
