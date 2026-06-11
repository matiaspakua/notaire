-- =============================================================================
-- V10__seed_workflow_demo_data.sql
-- =============================================================================
-- Author: Matias Miguez
-- Date: 2026-06-11
-- Description: Seeds the standard gestión workflow (definition, nodes,
--              transitions), assigns it to the main tipos de trámite, and adds
--              sample gestiones with trámites and historial so the dashboard
--              workflow tracker (CU70/CU71 — issue #453) renders end-to-end.
--              Mirrors the demo data added to init-db/02-data.sql.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- UP Migration
-- -----------------------------------------------------------------------------

INSERT INTO workflow_definition (version, id_workflow_definition, nombre, descripcion, activo) VALUES
(0, 1, 'Workflow de Gestión Estándar', 'Ciclo de vida estándar de una gestión de escritura', true)
ON CONFLICT (id_workflow_definition) DO NOTHING;

SELECT setval('workflow_definition_id_workflow_definition_seq', (SELECT MAX(id_workflow_definition) FROM workflow_definition));

INSERT INTO workflow_node (version, id_workflow_node, fk_workflow_definition_id, fk_estado_gestion_id, tipo) VALUES
(0, 1, 1, 1, 'INITIAL'),
(0, 2, 1, 2, 'INTERMEDIATE'),
(0, 3, 1, 3, 'INTERMEDIATE'),
(0, 4, 1, 7, 'INTERMEDIATE'),
(0, 5, 1, 6, 'INTERMEDIATE'),
(0, 6, 1, 10, 'FINAL'),
(0, 7, 1, 4, 'FINAL')
ON CONFLICT (id_workflow_node) DO NOTHING;

SELECT setval('workflow_node_id_workflow_node_seq', (SELECT MAX(id_workflow_node) FROM workflow_node));

INSERT INTO workflow_transition (version, id_workflow_transition, fk_workflow_definition_id, fk_nodo_origen_id, fk_nodo_destino_id, condicion, descripcion) VALUES
(0, 1, 1, 1, 2, NULL, 'Se asignan trámites y comienza la gestión'),
(0, 2, 1, 2, 3, NULL, 'Toda la documentación requerida fue presentada'),
(0, 3, 1, 3, 4, NULL, 'Se redacta la escritura'),
(0, 4, 1, 4, 5, NULL, 'Las partes firman la escritura'),
(0, 5, 1, 5, 6, NULL, 'La escritura se inscribe en el registro'),
(0, 6, 1, 3, 7, NULL, 'La gestión se archiva sin escritura')
ON CONFLICT (id_workflow_transition) DO NOTHING;

SELECT setval('workflow_transition_id_workflow_transition_seq', (SELECT MAX(id_workflow_transition) FROM workflow_transition));

UPDATE tipos_de_tramite SET fk_workflow_definition_id = 1
WHERE id_tipo_tramite IN (1, 2, 3) AND fk_workflow_definition_id IS NULL;

INSERT INTO gestiones_de_escrituras (version, id_gestion, numero, fecha_inicio, encabezado, observaciones, fk_id_persona_escribano, fk_id_estado_de_gestion) VALUES
(0, 1, 1001, '2026-05-02', 'Compraventa Lote 12 — Familia Pérez', NULL, 1, 3),
(0, 2, 1002, '2026-05-20', 'Donación — Sucesión Gómez', NULL, 1, 2)
ON CONFLICT (id_gestion) DO NOTHING;

SELECT setval('gestiones_de_escrituras_id_gestion_seq', (SELECT MAX(id_gestion) FROM gestiones_de_escrituras));

INSERT INTO tramites (version, id_tramite, numero, nombre, observaciones, fk_id_tipo_tramite, fk_id_gestion) VALUES
(0, 1, 1, 'Compraventa inmueble Lote 12', NULL, 1, 1),
(0, 2, 2, 'Donación a herederos', NULL, 2, 2)
ON CONFLICT (id_tramite) DO NOTHING;

SELECT setval('tramites_id_tramite_seq', (SELECT MAX(id_tramite) FROM tramites));

INSERT INTO historial (version, id_historial, fecha, observaciones, fk_id_gestion, fk_id_estado_gestion) VALUES
(0, 1, '2026-05-02', 'Apertura de la gestión', 1, 1),
(0, 2, '2026-05-09', 'Trámite de compraventa en curso', 1, 2),
(0, 3, '2026-05-28', 'Documentación completa recibida', 1, 3),
(0, 4, '2026-05-20', 'Apertura de la gestión', 2, 1),
(0, 5, '2026-06-01', 'Trámite de donación en curso', 2, 2)
ON CONFLICT (id_historial) DO NOTHING;

SELECT setval('historial_id_historial_seq', (SELECT MAX(id_historial) FROM historial));

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT COUNT(*) FROM workflow_node;          -- expect >= 7
-- SELECT COUNT(*) FROM workflow_transition;    -- expect >= 6
-- SELECT COUNT(*) FROM historial;              -- expect >= 5
