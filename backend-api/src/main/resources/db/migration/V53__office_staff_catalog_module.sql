-- =============================================================================
-- V53__office_staff_catalog_module.sql
-- =============================================================================
-- Issue #1683 / #1963 / CU78 / ADR-029. Catalog list/read APIs map to the
-- ``administracion`` module. Office Staff historically could read catalogs
-- (#559); without this grant, module enforcement returns 403 on GET.
-- Catalog writes stay ADMIN-only via Spring Security ``hasRole(ADMIN)``.
-- =============================================================================

INSERT INTO role_modules (fk_id_role, module)
SELECT r.id, 'administracion'
  FROM roles r
 WHERE r.name = 'Office Staff'
   AND NOT EXISTS (
         SELECT 1
           FROM role_modules m
          WHERE m.fk_id_role = r.id
            AND m.module = 'administracion'
       );
