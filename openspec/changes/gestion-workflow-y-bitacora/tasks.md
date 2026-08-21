> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #833 exists, labeled, and linked to CU13/CU83/CU16
- [ ] 1.2 Use Case documentation (CU13, CU83, CU16) exists and is accurate
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/gestion-workflow-transicion/spec.md`, `specs/gestion-bitacora/spec.md`, `specs/gestion-archive-debt-check/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 ADR: n/a — reutiliza el motor de workflow existente (ADR-014), sigue el layering `service`/`api` sobre `negocio`/`repository`, no introduce un patrón arquitectónico nuevo (design.md — Decisions)
- [ ] 1.6 Move Issue #833 to IN PROGRESS (`gh issue edit 833 --add-label "in-progress"`)

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/833_gestion-workflow-y-bitacora`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md` (happy path, edge cases, error paths)
- [ ] 3.2 Write unit tests: `GestionTransitionServiceTest`, `GestionBitacoraServiceTest`; extend `GestionArchiveDebtServiceTest` with the two new transition-validation scenarios
- [ ] 3.3 Write integration tests: `GestionTransitionControllerIntegrationTest`, `GestionBitacoraControllerIntegrationTest`; extend `GestionArchiveDebtControllerIntegrationTest`
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=GestionTransitionServiceTest,GestionBitacoraServiceTest,GestionArchiveDebtServiceTest`
- [ ] 3.5 Confirm every `#### Scenario:` in the three delta specs maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [ ] 4.1 `GestionTransitionService`: validar estado origen/destino contra `WorkflowTransitionRepository`/`WorkflowNodeRepository` del `WorkflowDefinition` del tipo de trámite de la gestión
- [ ] 4.2 `POST /api/v1/gestiones/{id}/transicionar` en `GestionController`, documentado en OpenAPI (`@Operation`, `@ApiResponse`)
- [ ] 4.3 `GestionBitacoraService`: escribir `Historial` (estado, fecha, observaciones) en alta, transición válida y archivado
- [ ] 4.4 Invocar `GestionBitacoraService` desde el alta de gestión (`GestionController.create()`/servicio equivalente) y desde `GestionTransitionService` tras aplicar una transición válida
- [ ] 4.5 `GET /api/v1/gestiones/{id}/historial` en `GestionController`, documentado en OpenAPI, delegando a `GestionBitacoraService`
- [ ] 4.6 `GestionArchiveDebtService.archivar`: delegar la validación de transición a `GestionTransitionService` (destino "Archivada") antes de aplicar el archivado, y llamar a `GestionBitacoraService` al confirmar
- [ ] 4.7 Confirmar que el cálculo/advertencia de saldo pendiente (issue #819) no cambia de comportamiento
- [ ] 4.8 Frontend: acción "Cambiar estado" en `frontend/src/app/dashboard/gestiones`, con selector limitado a los destinos válidos devueltos por el backend, usando `FormContainer`/`FormSection`/`FormField`/`FormActions`
- [ ] 4.9 Frontend: vista de bitácora que lista el `Historial` de la gestión seleccionada (CU13)
- [ ] 4.10 Frontend: manejar el error de archivado rechazado por transición inválida con un mensaje visible, reutilizando el patrón existente de `ConfirmDialog`
- [ ] 4.11 Confirmar en Swagger UI que los dos endpoints nuevos aparecen correctamente documentados

## 5. Actualizar tests existentes

- [ ] 5.1 Confirmar que los tests existentes de `GestionController` (CRUD genérico, `PUT /{id}`) no cambian — este change es aditivo salvo dentro de `archivar` (design.md — Regression Strategy)
- [ ] 5.2 Extender `GestionArchiveDebtServiceTest`/`GestionArchiveDebtControllerIntegrationTest` con los dos escenarios nuevos de validación de transición, sin debilitar las aserciones existentes de saldo pendiente
- [ ] 5.3 n/a — ningún test queda obsoleto

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add `frontend/tests/e2e/gestion-cambiar-estado.spec.ts`, `gestion-bitacora.spec.ts` (design.md — Playwright Strategy)
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the two new/changed screens at 320px, 768px and 1024px
- [ ] 7.4 Golden path + edge/error paths from design.md — Playwright Strategy covered

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md`, `CU16 – Archivar Gestión.md`, `CU83 – Definir Workflow de Estados y Transiciones.md` (proposal.md — Documentation Impact)
- [ ] 8.2 Update OpenAPI/Swagger annotations for the two new endpoints and verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — validación de transición y bitácora conectadas al flujo real de gestión
- [ ] 8.4 Archive superseded documents into `docs/archive/` — n/a, no document is superseded by this change
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #833`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/833_gestion-workflow-y-bitacora`
- [ ] 10.2 Open the PR titled `[#833] feat: conectar workflow y bitácora al flujo real de gestión`, referencing Issue #833 and CU13/CU83/CU16
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test from design.md — Deployment Strategy (transicionar una gestión, confirmar bitácora, archivar otra gestión; `GET /actuator/health` en verde)
- [ ] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available
- [ ] 12.3 Close GitHub Issue #833, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive gestion-workflow-y-bitacora`

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
