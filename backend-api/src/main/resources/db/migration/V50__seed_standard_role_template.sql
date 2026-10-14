-- =============================================================================
-- V50__seed_standard_role_template.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-10-14
-- Description: Issue #1682 / CU78 / ADR-029. Each installation is a single tenant that
--              starts from a standard role template and customizes it later. Seeds
--              Administrator, Notary and Office Staff with their module grants.
--              Insert-if-absent: a role is created only when no role has that name, and
--              grants are inserted only for the roles created here, so a role the tenant
--              already has keeps its own grants and description. Flyway runs this once.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

WITH template (name, description) AS (
    VALUES
        ('Administrator', 'Full access, including users, roles and audit'),
        ('Notary', 'Files, deeds, budgets, people, workflows and audit'),
        ('Office Staff', 'Day-to-day files, deeds, budgets and people')
),
created AS (
    INSERT INTO roles (version, name, description, active)
    SELECT 0, t.name, t.description, true
      FROM template t
     WHERE NOT EXISTS (SELECT 1 FROM roles r WHERE r.name = t.name)
    RETURNING id, name
),
grants (role_name, module) AS (
    VALUES
        ('Administrator', 'administracion'), ('Administrator', 'usuarios'), ('Administrator', 'tramites'),
        ('Administrator', 'presupuestos'), ('Administrator', 'escrituras'), ('Administrator', 'personas'),
        ('Administrator', 'auditoria'), ('Administrator', 'workflows'),
        ('Notary', 'tramites'), ('Notary', 'presupuestos'), ('Notary', 'escrituras'),
        ('Notary', 'personas'), ('Notary', 'auditoria'), ('Notary', 'workflows'),
        ('Office Staff', 'tramites'), ('Office Staff', 'presupuestos'), ('Office Staff', 'escrituras'),
        ('Office Staff', 'personas')
)
INSERT INTO role_modules (fk_id_role, module)
SELECT c.id, g.module
  FROM created c
  JOIN grants g ON g.role_name = c.name;

UPDATE users
   SET fk_id_role = (SELECT id FROM roles WHERE name = 'Administrator')
 WHERE lower(username) = 'admin'
   AND fk_id_role IS NULL;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT r.name, count(m.module) FROM roles r LEFT JOIN role_modules m ON m.fk_id_role = r.id GROUP BY r.name;
