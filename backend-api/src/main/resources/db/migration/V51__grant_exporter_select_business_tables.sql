-- =============================================================================
-- V51__grant_exporter_select_business_tables.sql
-- =============================================================================
-- Author: Cursor Agent
-- Date: 2026-10-15
-- Description: #1746 / CU77 — grant the postgres-exporter role SELECT on the
-- four Notaire business tables used by extend-query gauges in
-- infra/observability/prometheus/postgres_exporter.yml. V12 only granted
-- pg_monitor (stats views); SELECT on application tables was denied every
-- scrape, emptying the Notaire Business Metrics panels and spamming ERROR.
--
-- ${exporterUsername} is the Flyway placeholder from
-- spring.flyway.placeholders.exporterUsername (POSTGRES_EXPORTER_USER).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

GRANT SELECT ON TABLE deeds TO ${exporterUsername};
GRANT SELECT ON TABLE budgets TO ${exporterUsername};
GRANT SELECT ON TABLE people TO ${exporterUsername};
GRANT SELECT ON TABLE users TO ${exporterUsername};

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT grantee, table_name, privilege_type
-- FROM information_schema.role_table_grants
-- WHERE grantee = '${exporterUsername}'
--   AND table_name IN ('deeds', 'budgets', 'people', 'users');
