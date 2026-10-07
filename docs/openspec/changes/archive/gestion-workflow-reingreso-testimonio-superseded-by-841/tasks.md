> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #841 exists, labeled, and linked to Use Cases CU83 (#451, #453, #454, #455), CU06 (#159), CU07 (#160), CU11 (#164), CU44 (#197)
- [ ] 1.2 Use Case documentation exists at `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md`; update it before implementing to document that the standard workflow now covers the post-firma circuit up to inscripción, and that reingreso is shown as a movement, not a node
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/workflow-testimonio-movimiento-tracker/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`; confirm #832 (`escritura-post-firma-legal-cycle`) and #833 (`gestion-workflow-y-bitacora`) are merged before starting Group 4 — this change's new states/nodes are inert without #833's transition-writing service, and its movements are only populated by #832's business actions (see design.md — Riesgos / Migration Plan)
- [ ] 1.5 n/a — no ADR required; this change extends the existing workflow engine's seed data and read-only trace under [ADR-014](../../200-architecture/202-ADR/ADR-014-workflow-engine.md), it does not introduce a new architectural pattern (see design.md — Decisions)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 841 --add-label "in-progress"`)

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/841_gestion-workflow-reingreso-testimonio`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: happy path, edge cases, error paths (see design.md — Testing Strategy, 9 scenarios)
- [ ] 3.2 Write unit tests in `WorkflowTraceServiceTest`: `shouldIncludeMovimientosTestimonioWhenTestimonioHasMovements`, `shouldNotIncludeMovimientosWhenNoTestimonio`, `shouldIncludeAllMovimientosInChronologicalOrder`, `shouldDegradeGracefullyWhenPostFirmaNodesMissing`
- [ ] 3.3 Write integration tests confirming the seed migration's node/transition wiring for `WorkflowDefinition` id 1 (new `WorkflowSeedDataIntegrationTest` or extend `WorkflowNodeRepositoryIntegrationTest`): expected estado ids 11–13 present, no dangling final node, node 6/transition 5 removed
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=WorkflowTraceServiceTest,WorkflowSeedDataIntegrationTest`
- [ ] 3.5 Confirm every `#### Scenario:` in `specs/workflow-testimonio-movimiento-tracker/spec.md` maps to at least one test (cross-check against traceability.md — Requirement coverage)

## 4. Implementación

- [ ] 4.1 Crear migración Flyway `V17__extend_workflow_post_firma_testimonio.sql`: insertar `EstadoDeGestion` 11 ("Testimonio Generado"), 12 ("Testimonio Ingresado a Inscripción"), 13 ("Testimonio Retirado")
- [ ] 4.2 En la misma migración, insertar `workflow_node` 8 (INTERMEDIATE, estado 11), 9 (INTERMEDIATE, estado 12), 10 (FINAL, estado 13) para `workflow_definition` id 1; insertar `workflow_transition` conectando node 5 (Firmada) → node 8 → node 9 → node 10
- [ ] 4.3 En la misma migración, eliminar el `workflow_transition` id 5 y el `workflow_node` id 6 (Firmada → Inscripta directo), ahora superseded (ver design.md — Decisions); dejar la fila `estados_de_gestion` id 10 intacta (referencia inerte, sin otras dependencias)
- [ ] 4.4 Ejecutar `mvn test -Ppg-integration` para validar que Flyway aplica `V17` limpio contra un esquema vacío
- [ ] 4.5 En `WorkflowTraceService.buildTrace`, resolver el `Testimonio` vigente de la gestión (primer `Tramite` con `Escritura` no nula cuyo `testimonioList` no esté vacío) y su `movimientoTestimonioList`, ordenado por `fechaIngreso`
- [ ] 4.6 Agregar `DtoMovimientoTestimonioEntry` (clase anidada en `DtoGestionWorkflowTrace`, `notaire-shared`) con `fechaIngreso`, `fechaSalida`, `fechaInscripcion`, `volvioObservado` (derivado: `fechaSalida != null && !inscripta`); agregar `List<DtoMovimientoTestimonioEntry> movimientosTestimonio` a `DtoGestionWorkflowTrace`, vacía cuando no hay testimonio
- [ ] 4.7 En `frontend/src/types/index.ts`, agregar `MovimientoTestimonioEntry` y el campo `movimientosTestimonio` a `GestionWorkflowTrace`
- [ ] 4.8 En `WorkflowTracker.tsx`, renderizar `movimientosTestimonio` como línea de tiempo secundaria asociada al nodo "Testimonio Ingresado a Inscripción" (estado 12), con un indicador de conteo de reingresos (`movimientos.filter(m => m.volvioObservado).length`) visible solo cuando es mayor a cero
- [ ] 4.9 Confirmar que, para un `WorkflowDefinition` sin los nodos post-firma (estado 11/12/13 ausentes), el tracker se degrada al comportamiento actual sin error (guard clause en el cálculo de la línea de movimientos)

## 5. Actualizar tests existentes

- [ ] 5.1 Confirmar que los tests existentes de `WorkflowTraceServiceTest` para `computeNodeStatuses` y la forma actual de `nodes`/`transitions`/`historial` siguen pasando sin cambios — esta extensión solo agrega un campo nuevo al DTO y una rama de código cuando existe testimonio
- [ ] 5.2 Confirmar que los tests de `computeNodeStatuses` para las dos gestiones demo de `V10` (ids 1 y 2) siguen devolviendo el mismo estado tras eliminar `workflow_node` id 6 (ninguna alcanza estado 6/10 en su `Historial` — ver design.md — Riesgos)
- [ ] 5.3 n/a — ningún test queda obsoleto por este cambio; `jpa.MovimientoTestimonioJpaController` y `jpa.EstadoDeGestionJpaController` no se modifican

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `mvn test -Ppg-integration` — Flyway schema validation guard test
- [ ] 6.5 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.6 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Extend `frontend/tests/e2e/workflow-tracker.spec.ts` (see design.md — Playwright Strategy): golden path (testimonio con reingresos muestra línea de movimientos y contador) + edge paths (sin reingresos no muestra indicador; workflow sin nodos post-firma se degrada sin error)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the animated diagram at 320px, 768px and 1024px
- [ ] 7.4 n/a — this change has a UI surface, covered above

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Actualizar `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` (workflow estándar cubre ahora el circuito post-firma hasta inscripción; el reingreso se muestra como movimiento, no como nodo)
- [ ] 8.2 Actualizar `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` (línea de tiempo secundaria de movimientos de testimonio)
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — el diagrama animado de la gestión ahora refleja el circuito post-firma, incluidos los reingresos de un testimonio observado
- [ ] 8.4 n/a — no superseded documents to archive
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #841`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/841_gestion-workflow-reingreso-testimonio`
- [ ] 10.2 Open the PR titled `[#841] feat(workflow): reingreso de testimonio en el trace y el diagrama animado`, referencing Issue #841 and Use Case CU83 (plus CU06/CU07/CU11/CU44)
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment (see design.md — Deployment Strategy): `GET /api/v1/gestiones/{id}/workflow-trace` for a gestión on the standard workflow returns the three new nodes and an empty `movimientosTestimonio`; open the dashboard and confirm the animated diagram renders for a gestión on the standard workflow and one without the post-firma nodes; `GET /actuator/health` en verde
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue #841
- [ ] 12.4 Archive the change: `openspec archive gestion-workflow-reingreso-testimonio`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
