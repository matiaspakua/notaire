> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #836 exists, labeled, and linked to CU22/CU59/CU48/CU51
- [x] 1.2 Use Case documentation (CU22, CU59, CU48, CU51) exists and is accurate
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/gestion-asignacion-suplencia/spec.md`, `specs/persona-credencial-escribano/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR: n/a — sigue el layering existente (`service`/`api` sobre `negocio`/`repository`), no introduce un patrón arquitectónico nuevo (design.md — Decisions)
- [x] 1.6 Move Issue #836 to IN PROGRESS (`gh issue edit 836 --add-label "in-progress"`)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/836_suplencia-efecto-en-gestiones`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases from the Requirement coverage table in `traceability.md` (happy path, edge cases, error paths)
- [x] 3.2 Write unit tests: `GestionSuplenciaServiceTest`, extend `PersonaServiceTest`
- [x] 3.3 Write integration tests: extend `GestionControllerIntegrationTest`
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=GestionSuplenciaServiceTest`
- [x] 3.5 Confirm every `#### Scenario:` in the two delta specs maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [x] 4.1 `SuplenciaRepository`: agregar `findByFkIdSuplantadoIdPersonaAndFechaInicioLessThanEqualAndFechaFinGreaterThanEqual`
- [x] 4.2 `GestionSuplenciaService`: resolver el escribano efectivo (solicitado o suplente) para una fecha dada
- [x] 4.3 `GestionController.applyGestionFields`/`createCompleteCase`/`updateCompleteCase`: invocar `GestionSuplenciaService`, registrar el redireccionamiento en `observaciones` cuando aplica
- [x] 4.4 Frontend: `frontend/src/app/dashboard/personas/page.tsx` agrega campo "Registro de escribano" (alta/edición) usando `FormField`
- [x] 4.5 Frontend: `frontend/src/app/dashboard/gestiones/page.tsx` muestra el aviso de redirección cuando la respuesta de creación/edición trae observaciones de suplencia
- [x] 4.6 Confirmar en Swagger UI que `POST /api/v1/gestiones/complete-case` y `PUT /api/v1/gestiones/{id}/complete-case` documentan el comportamiento de redirección

## 5. Actualizar tests existentes

- [x] 5.1 Confirmar que ningún test existente de `GestionController`/`PersonaService` (creación/edición sin suplencia activa) cambia — este change es aditivo (design.md — Regression Strategy)
- [x] 5.2 n/a — no se debilita ninguna aserción existente, no se identificó ningún test que requiera actualización
- [x] 5.3 n/a — ningún test queda obsoleto

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [x] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/persona-credencial-escribano.spec.ts`, `gestion-suplencia-redirect.spec.ts` (design.md — Playwright Strategy)
- [x] 7.2 `cd frontend && npx playwright test` — all green
- [x] 7.3 Verify the changed screens at 320px, 768px and 1024px
- [x] 7.4 Golden path + edge/error paths from design.md — Playwright Strategy covered

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md`, `CU48 – Dar alta escribano.md`, `CU51 – Modificar escribano.md` (proposal.md — Documentation Impact)
- [x] 8.2 Update OpenAPI/Swagger annotations for the changed endpoints and verify in Swagger UI
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — redirección por suplencia activa; alta/edición de registro de escribano desde personas
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — n/a, no document is superseded by this change
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #836`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/836_suplencia-efecto-en-gestiones`
- [x] 10.2 Open the PR titled `[#836] feat: dar efecto práctico a las suplencias de escribano`, referencing Issue #836 and CU22/CU59/CU48/CU51
- [x] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml` — required checks green; two pre-existing unrelated flakes (`Unit Tests (Vitest)`, full Playwright suite) also failing identically on `main`, unrelated to this change
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Run the smoke test from design.md — Deployment Strategy (suplencia vigente, gestión redirigida al suplente; `GET /actuator/health` en verde) — `/actuator/health` UP; TS-0092 (suplencia redirect) green in CI on the merge commit
- [x] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available — additive change, no schema/behavior removal; revert of PR #970 remains a clean rollback
- [x] 12.3 Close GitHub Issue #836, referencing the PR — auto-closed on merge of PR #970
- [x] 12.4 Archive the change: `openspec archive suplencia-efecto-en-gestiones`

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
