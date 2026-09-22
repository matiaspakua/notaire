-- =============================================================================
-- V33__fix_notebooks_year_column_and_entity_mapping.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-21
-- Description: Corrects a bug introduced in V26 (Slice 1 of #973). V26
-- renamed notebooks.anio to "year", but Notebook.java's own @Column
-- annotation was never updated to match (it still declared "anio"),
-- so the entity has been silently out of sync with the real schema on
-- Postgres since V26 merged. The bug went undetected because the H2
-- unit-test profile builds its schema from the (stale) JPA annotations
-- via ddl-auto=create rather than Flyway, and FlywaySchemaValidation-
-- IntegrationTest does not happen to exercise the notebooks endpoint.
--
-- Discovered while implementing Slice 7 (V32), when renaming folios.anio
-- to the literal "year" failed with an H2 syntax error: YEAR is an H2
-- reserved keyword. The same literal was already live on notebooks via
-- V26, just never actually created by H2 (masking the collision) and
-- never queried through the broken Java mapping on Postgres either.
--
-- This migration renames "year" to "year_number" (matching the same
-- non-reserved name chosen for folios in V32), and Notebook.java's
-- @Column annotation is corrected in the same commit.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE notebooks RENAME COLUMN year TO year_number;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'notebooks' AND column_name = 'year_number';
-- Expect: 1 row.
