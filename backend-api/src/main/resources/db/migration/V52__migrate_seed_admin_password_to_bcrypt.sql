-- =============================================================================
-- V52__migrate_seed_admin_password_to_bcrypt.sql
-- =============================================================================
-- Author: Cursor Agent
-- Date: 2026-10-15
-- Description: #1762 / CU78 / RNF-14 — hard cutoff for legacy MD5 password
--              verification (java:S4790). Replace the V2 seed hash MD5("admin")
--              with a BCrypt cost-12 hash of the same plaintext so existing
--              installs keep a working admin login after UserController drops
--              MessageDigest MD5. Historical V2 script content is left unchanged
--              (FlywayMigrationScriptTest asserts the original seed text).
--
-- BCrypt("admin", cost=12) =
--   $2a$12$MzGVbtKng5BRII/pfWCJpOE.hkzzYSLroZOjzATlTdRyygKG8Z0e6
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

UPDATE users
   SET password = '$2a$12$MzGVbtKng5BRII/pfWCJpOE.hkzzYSLroZOjzATlTdRyygKG8Z0e6'
 WHERE password = '21232f297a57a5a743894a0e4a801fc3';

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT username, left(password, 4) AS prefix
--   FROM users
--  WHERE lower(username) = 'admin';
-- Expect prefix = '$2a$' (or '$2b$' / '$2y$') and zero rows still holding the MD5 hex.
