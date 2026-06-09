-- =============================================================================
-- V7__add_workflow_tables.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-06-09
-- Description: Add workflow_definition, workflow_node, and workflow_transition
--              tables to support directed-graph workflow management for
--              Estados de Gestión (issues #436, #450 — CU70, CU71).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS workflow_definition (
    version integer NOT NULL DEFAULT 0,
    id_workflow_definition SERIAL PRIMARY KEY,
    nombre text NOT NULL,
    descripcion text,
    activo boolean NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS workflow_node (
    version integer NOT NULL DEFAULT 0,
    id_workflow_node SERIAL PRIMARY KEY,
    fk_workflow_definition_id integer NOT NULL
        REFERENCES workflow_definition(id_workflow_definition) ON DELETE CASCADE,
    fk_estado_gestion_id integer NOT NULL
        REFERENCES estados_de_gestion(id_estado_gestion),
    tipo text NOT NULL CHECK (tipo IN ('INITIAL', 'INTERMEDIATE', 'FINAL')),
    posicion_x real,
    posicion_y real
);

CREATE TABLE IF NOT EXISTS workflow_transition (
    version integer NOT NULL DEFAULT 0,
    id_workflow_transition SERIAL PRIMARY KEY,
    fk_workflow_definition_id integer NOT NULL
        REFERENCES workflow_definition(id_workflow_definition) ON DELETE CASCADE,
    fk_nodo_origen_id integer NOT NULL
        REFERENCES workflow_node(id_workflow_node) ON DELETE CASCADE,
    fk_nodo_destino_id integer NOT NULL
        REFERENCES workflow_node(id_workflow_node) ON DELETE CASCADE,
    condicion text,
    descripcion text
);

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM workflow_definition;
-- SELECT COUNT(*) FROM workflow_node;
-- SELECT COUNT(*) FROM workflow_transition;
