-- =============================================================================
-- V6__make_documentos_presentados_tramite_optional.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-06-05
-- Description: Make fk_id_tramite nullable in documentos_presentados so that
--              standalone documents can be created from the Documentos UI
--              without requiring a tramite association upfront.
--              Also relaxes reingresado to allow NULL for the same reason.
-- =============================================================================

-- Allow fk_id_tramite to be NULL
ALTER TABLE documentos_presentados
    ALTER COLUMN fk_id_tramite DROP NOT NULL;

-- Allow reingresado to be NULL (entity field is Boolean, not primitive)
ALTER TABLE documentos_presentados
    ALTER COLUMN reingresado DROP NOT NULL;
