-- =============================================================================
-- V13__make_tramites_nombre_numero_optional.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-08-05
-- Description: Make nombre and numero nullable in tramites. Tramite.java has no
--              mapped nombre/numero field, so Hibernate never populates them;
--              the NOT NULL constraints from V1 reject every insert made through
--              the entity (e.g. the CU02 /complete-case endpoint) against
--              PostgreSQL, even though H2-backed unit tests never touch DDL.
-- =============================================================================

ALTER TABLE tramites
    ALTER COLUMN nombre DROP NOT NULL;

ALTER TABLE tramites
    ALTER COLUMN numero DROP NOT NULL;
