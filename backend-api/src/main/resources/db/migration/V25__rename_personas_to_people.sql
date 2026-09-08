-- =============================================================================
-- V25__rename_personas_to_people.sql
-- =============================================================================
-- Author: Claude (paired with Matias)
-- Date: 2026-09-08
-- Description: First vertical slice of the domain-to-English rename epic
-- (#973, issue #974). Renames the "personas" table and its own columns to
-- English. FK columns on OTHER tables that reference this table (e.g.
-- fk_id_persona_cliente, fk_id_persona_escribano) are intentionally left
-- untouched here -- they belong to the owning entity's own future rename
-- slice, per the epic's vertical-slice strategy. PostgreSQL automatically
-- keeps FK constraints valid across a table rename.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

ALTER TABLE personas RENAME TO people;

ALTER TABLE people RENAME COLUMN id_persona TO id;
ALTER TABLE people RENAME COLUMN nombre TO first_name;
ALTER TABLE people RENAME COLUMN apellido TO last_name;
ALTER TABLE people RENAME COLUMN nacionalidad TO nationality;
ALTER TABLE people RENAME COLUMN numero_identificacion TO identification_number;
ALTER TABLE people RENAME COLUMN cuit TO tax_id;
ALTER TABLE people RENAME COLUMN sexo TO sex;
ALTER TABLE people RENAME COLUMN estado_civil TO marital_status;
ALTER TABLE people RENAME COLUMN numero_nupcias TO marriage_count;
ALTER TABLE people RENAME COLUMN ocupacion TO occupation;
ALTER TABLE people RENAME COLUMN domicilio TO address;
ALTER TABLE people RENAME COLUMN telefono TO phone;
ALTER TABLE people RENAME COLUMN registro_escribano TO notary_registration_number;
ALTER TABLE people RENAME COLUMN es_cliente TO is_client;

-- Columns not mapped by the JPA entity (pre-existing schema/entity drift,
-- unrelated to this rename) are translated too for table-wide consistency.
-- The pre-existing duplicate "email" column is disambiguated as
-- "secondary_email" FIRST, freeing up the "email" name for e_mail below.
ALTER TABLE people RENAME COLUMN localidad TO locality;
ALTER TABLE people RENAME COLUMN provincia TO province;
ALTER TABLE people RENAME COLUMN celular TO mobile_phone;
ALTER TABLE people RENAME COLUMN email TO secondary_email;
ALTER TABLE people RENAME COLUMN e_mail TO email;
ALTER TABLE people RENAME COLUMN profesion TO profession;
ALTER TABLE people RENAME COLUMN observaciones TO notes;
ALTER TABLE people RENAME COLUMN es_escribano TO is_notary;

-- Sequence created implicitly for the SERIAL id_persona column is renamed to
-- keep it consistent with the new column/table names.
ALTER SEQUENCE IF EXISTS personas_id_persona_seq RENAME TO people_id_seq;

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM people;
