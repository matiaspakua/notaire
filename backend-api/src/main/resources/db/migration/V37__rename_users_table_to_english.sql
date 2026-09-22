-- =============================================================================
-- V37__rename_users_table_to_english.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-22
-- Description: Slice 11 of the domain-schema-to-English rename (#973,
-- openspec/changes/translate-domain-schema-to-english). Renames usuarios
-- and its own columns to English, matching the field names already used
-- by the corresponding (already English) JPA entity: User.
--
-- "usuarios" was missed by explore.md's original table inventory (a scope
-- gap in the epic's own audit, discovered only after all 10 planned
-- slices merged) -- it is a genuine, previously undiscovered remaining
-- Spanish table, not a new requirement.
--
-- fk_id_persona and fk_id_rol ARE renamed (to fk_id_person, fk_id_role):
-- their target tables (people, roles) are already English. Also fixes
-- referencedColumnName in AuditRecord, whose own (unrenamed) fk_id_usuario
-- column still pointed at the old id_usuario PK name.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

-- Table: usuarios -> users
ALTER TABLE usuarios RENAME TO users;
ALTER TABLE users RENAME COLUMN id_usuario TO id;
ALTER TABLE users RENAME COLUMN nombre TO username;
ALTER TABLE users RENAME COLUMN contrasenia TO password;
ALTER TABLE users RENAME COLUMN estado TO status;
ALTER TABLE users RENAME COLUMN tipo TO user_type;
ALTER TABLE users RENAME COLUMN fk_id_persona TO fk_id_person;
ALTER TABLE users RENAME COLUMN fk_id_rol TO fk_id_role;
ALTER SEQUENCE IF EXISTS usuarios_id_usuario_seq RENAME TO users_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM users;
