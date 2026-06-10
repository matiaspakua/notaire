-- =============================================================================
-- V9__add_roles_permisos.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-06-10
-- Description: Adds roles and role_permissions tables; links users to roles (CU431)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS roles (
    version integer NOT NULL DEFAULT 0,
    id_rol SERIAL PRIMARY KEY,
    nombre text NOT NULL,
    descripcion text,
    activo boolean NOT NULL DEFAULT true,
    CONSTRAINT roles_nombre_unique UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS roles_permisos (
    fk_id_rol integer NOT NULL REFERENCES roles(id_rol) ON DELETE CASCADE,
    modulo text NOT NULL,
    PRIMARY KEY (fk_id_rol, modulo)
);

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS fk_id_rol integer REFERENCES roles(id_rol);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM roles;
-- SELECT COUNT(*) FROM roles_permisos;
