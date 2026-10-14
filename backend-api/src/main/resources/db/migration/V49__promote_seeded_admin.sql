-- =============================================================================
-- V49__promote_seeded_admin.sql
-- =============================================================================
-- Author: Claude Code
-- Date: 2026-10-13
-- Description: Issue #1638 / CU78 / SR-16. The Escribano (notary) user type no longer
--              carries administrator authority. V2 stored the seeded account `admin`
--              as Escribano, so it is promoted to ADMIN here and nobody is
--              locked out. UPGRADE NOTE: any OTHER user stored as Escribano that was
--              used as an administrator becomes a plain notary; the admin account
--              promotes them again by setting their type to ADMIN.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

UPDATE users
   SET user_type = 'ADMIN'
 WHERE lower(username) = 'admin'
   AND lower(trim(user_type)) = 'escribano';

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- testing/database/checks/80_roles.sql
