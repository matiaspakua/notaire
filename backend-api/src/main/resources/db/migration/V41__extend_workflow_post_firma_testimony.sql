-- =============================================================================
-- V41__extend_workflow_post_firma_testimony.sql
-- =============================================================================
-- Author: Cursor Cloud Agent
-- Date: 2026-10-03
-- Description: Issue #841 / CU83 / CU06 / CU07 / CU11 / CU44 — strategy (b):
--              seed three post-signing ManagementStatus rows (Testimonio
--              Generado / Ingresado a Inscripción / Retirado), replace the
--              standard WorkflowDefinition Firmada→Inscripta path with linear
--              nodes Generado → Ingresado → Retirado (FINAL), and leave status
--              id 10 in place as inert catalog data. The unbounded reingreso
--              loop stays off-graph (TestimonyMovement secondary timeline).
-- =============================================================================

-- -----------------------------------------------------------------------------
-- New management statuses (ids 11–13)
-- -----------------------------------------------------------------------------
INSERT INTO management_statuses (version, id, name, notes) VALUES
(0, 11, 'Testimonio Generado', 'Post-signing: testimony generated from signed deed'),
(0, 12, 'Testimonio Ingresado a Inscripcion', 'Post-signing: testimony submitted for inscription'),
(0, 13, 'Testimonio Retirado', 'Post-signing: testimony withdrawn after inscription cycle')
ON CONFLICT (id) DO NOTHING;

SELECT setval('management_statuses_id_seq', (SELECT MAX(id) FROM management_statuses));

-- -----------------------------------------------------------------------------
-- Replace Firmada (node 5) → Inscripta (node 6) with post-signing linear path
-- Delete transition id 5 first (FK to node 6), then orphan FINAL node 6.
-- Status id 10 (Gestion con Escritura Inscripta) remains as inert catalog.
-- -----------------------------------------------------------------------------
DELETE FROM workflow_transition
WHERE id_workflow_transition = 5
  AND fk_workflow_definition_id = 1;

DELETE FROM workflow_node
WHERE id_workflow_node = 6
  AND fk_workflow_definition_id = 1;

INSERT INTO workflow_node (version, id_workflow_node, fk_workflow_definition_id, fk_estado_gestion_id, tipo) VALUES
(0, 8, 1, 11, 'INTERMEDIATE'),
(0, 9, 1, 12, 'INTERMEDIATE'),
(0, 10, 1, 13, 'FINAL')
ON CONFLICT (id_workflow_node) DO NOTHING;

SELECT setval('workflow_node_id_workflow_node_seq', (SELECT MAX(id_workflow_node) FROM workflow_node));

INSERT INTO workflow_transition (version, id_workflow_transition, fk_workflow_definition_id, fk_nodo_origen_id, fk_nodo_destino_id, condicion, descripcion) VALUES
(0, 5, 1, 5, 8, NULL, 'Testimony generated after deed signing'),
(0, 7, 1, 8, 9, NULL, 'Testimony submitted for inscription'),
(0, 8, 1, 9, 10, NULL, 'Testimony withdrawn after inscription cycle')
ON CONFLICT (id_workflow_transition) DO NOTHING;

SELECT setval('workflow_transition_id_workflow_transition_seq', (SELECT MAX(id_workflow_transition) FROM workflow_transition));

-- -----------------------------------------------------------------------------
-- Verification
-- -----------------------------------------------------------------------------
-- SELECT id, name FROM management_statuses WHERE id BETWEEN 11 AND 13;
-- SELECT id_workflow_node, fk_estado_gestion_id, tipo FROM workflow_node
--   WHERE fk_workflow_definition_id = 1 ORDER BY id_workflow_node;
-- SELECT id_workflow_transition, fk_nodo_origen_id, fk_nodo_destino_id
--   FROM workflow_transition WHERE fk_workflow_definition_id = 1
--   ORDER BY id_workflow_transition;
