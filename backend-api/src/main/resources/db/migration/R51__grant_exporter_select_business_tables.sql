-- =============================================================================
-- R51__grant_exporter_select_business_tables.sql
-- =============================================================================
-- Author: Cursor Agent
-- Date: 2026-10-15
-- Description: Rollback for V51 — revoke exporter SELECT on business tables.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- DOWN Migration (Rollback)
-- -----------------------------------------------------------------------------

REVOKE SELECT ON TABLE deeds FROM ${exporterUsername};
REVOKE SELECT ON TABLE budgets FROM ${exporterUsername};
REVOKE SELECT ON TABLE people FROM ${exporterUsername};
REVOKE SELECT ON TABLE users FROM ${exporterUsername};
