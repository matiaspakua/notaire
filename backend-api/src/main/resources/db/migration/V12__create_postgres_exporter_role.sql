-- =============================================================================
-- V12__create_postgres_exporter_role.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-07-24
-- Description: Creates a least-privilege PostgreSQL role for postgres_exporter
-- metrics scraping (issue #675). Previously postgres-exporter reused the
-- application's own admin datasource credentials in infra/docker-compose.yml,
-- so a compromised metrics exporter had full read/write access to every
-- application table. This role is granted only pg_monitor, PostgreSQL's
-- built-in read-only role for statistics views.
--
-- ${exporterUsername} / ${exporterPassword} are Flyway placeholders (see
-- spring.flyway.placeholders.* in application.properties), sourced from the
-- POSTGRES_EXPORTER_USER / POSTGRES_EXPORTER_PASSWORD env vars so the role
-- always matches the credentials infra/docker-compose.yml gives the exporter.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${exporterUsername}') THEN
        EXECUTE 'CREATE ROLE ${exporterUsername} WITH LOGIN PASSWORD ''${exporterPassword}''';
    ELSE
        EXECUTE 'ALTER ROLE ${exporterUsername} WITH LOGIN PASSWORD ''${exporterPassword}''';
    END IF;
END
$$;

GRANT pg_monitor TO ${exporterUsername};

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT rolname, rolcanlogin FROM pg_roles WHERE rolname = '${exporterUsername}';
-- SELECT * FROM pg_auth_members WHERE roleid = 'pg_monitor'::regrole;
