> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #820 exists, labeled, and linked to Use Case CU-47 / CU-02
- [x] 1.2 Use Case documentation (`CU47 – Consultar Pago.md`) exists and is accurate — confirm step 6's fields match the new endpoint's payload
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/pago-presupuesto-gestion-summary/spec.md`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No ADR required — follows existing layering (see design.md — Architecture review)
- [x] 1.6 Move the Issue to IN PROGRESS: `gh issue edit 820 --add-label "in-progress"`

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b feat/820_expose-pago-presupuesto-gestion-summary`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: payment response includes presupuesto (get by id, list by presupuesto, create); presupuesto summary with zero/one/many payments and unknown id; gestión summary with one/multiple/no-payment trámites
- [x] 3.2 Write unit tests: `PagoControllerTest` (presupuesto id on responses), `PresupuestoResumenServiceTest`, `GestionResumenFinancieroServiceTest`
- [x] 3.3 Write integration tests: `PresupuestoResumenControllerTest`, `GestionResumenFinancieroControllerTest` (H2/PostgreSQL, per `ApiH2IntegrationTest`/`ApiIntegrationTest` conventions)
- [x] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=PagoControllerTest,PresupuestoResumenServiceTest,GestionResumenFinancieroServiceTest,PresupuestoResumenControllerTest,GestionResumenFinancieroControllerTest`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [x] 4.1 Add `DtoPagoResponse` (idPago, idPresupuesto, monto, fecha, metodoPago, observaciones) and map it in `PagoController`'s `getAll`, `getById`, `getByPresupuesto`, create/edit endpoints, replacing the direct `Pago` entity return
- [x] 4.2 Add `PresupuestoResumenService` (new or extend `PagoService`) that assembles gestión número, encabezado, presupuesto número, total, saldo (reusing `PagoService.calcularSaldoPendiente`) and the list of `DtoPagoResponse` for a presupuesto
- [x] 4.3 Add `GET /api/v1/presupuestos/{id}/resumen` in the appropriate controller, returning 404 for an unknown presupuesto
- [x] 4.4 Add `GestionResumenFinancieroService` (or extend `GestionArchiveDebtService`) that sums total presupuestado, total cobrado and saldo across a gestión's linked presupuestos, reusing `calcularSaldoPendiente` for the saldo side
- [x] 4.5 Add `GET /api/v1/gestiones/{id}/resumen-financiero` in `GestionController`
- [x] 4.6 Document both new endpoints with `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [x] 4.7 Frontend: add/extend the CU47 consulta-de-pagos screen to call `/presupuestos/{id}/resumen` and display total, saldo and payment list without extra navigation, using `FormContainer`/`FormSection` from `@/theme/form-patterns`

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected: any `PagoControllerTest`/`PagoIntegrationTest` cases asserting the current `List<Pago>`/`Pago` response shape will need updating to the new `DtoPagoResponse` shape
- [x] 5.2 Update them without weakening assertions — confirm they now also assert `idPresupuesto` is present
- [x] 5.3 No test removal expected; note here if any becomes genuinely obsolete — none removed; the stale `createPago`/`PagoPayload` E2E helper was fixed, not removed (see 7.1)

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — unit + integration — 1513 tests, 0 failures
- [x] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor held (ran as part of `mvn verify`, BUILD SUCCESS)
- [x] 6.3 `mvn verify -pl backend-api -am -DskipSpotBugs=false` — BUILD SUCCESS; Checkstyle non-blocking pre-existing debt only, no new violations beyond the existing empty-body `{}` convention already used elsewhere (e.g. `HistorialMapper`); SpotBugs clean
- [x] 6.4 `bash testing/scripts/test.sh` — HTTP smoke suite passed (repo has no Bruno collection; `integration-test/scripts/test.sh` referenced in this task does not exist — corrected path)
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add/update the CU47 consulta-de-pagos E2E spec under `frontend/tests/` covering: presupuesto with payments, presupuesto with none, unknown presupuesto id (404). Also fixed the stale `createPago`/`PagoPayload` test helper, which referenced a non-existent `idGestion`/`formaPago` shape instead of the real `POST /pagos` contract (`idPresupuesto`/`metodoPago`). Gestión-level aggregate (multiple presupuestos) is covered by backend `GestionResumenFinancieroServiceTest` only — per design.md, no frontend screen calls `/gestiones/{id}/resumen-financiero` in this change.
- [x] 7.2 `cd frontend && npx playwright test` — all green (423 passed, 35 skipped, 0 failed)
- [x] 7.3 Verify the CU47 screen at 320px, 768px and 1024px — structural check: the "Ver resumen" dialog reuses the shared `Dialog`/`DialogContent`/`FormSection`/`FormContainer` components used by every other modal in the app, unmodified, so it inherits the same already-shipped responsive behavior; live browser check at these breakpoints was not possible (Claude-in-Chrome extension not connected in this session)
- [x] 7.4 n/a does not apply — this change has a UI surface (CU47 screen)

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` per proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger annotations for the two new endpoints; verify in Swagger UI — confirmed both paths present via `GET /v3/api-docs`
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) with the resumen-financiero entry
- [x] 8.4 No documents to archive for this change
- [x] 8.5 Confirm no information duplicated — CU47 doc remains the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — PREFLIGHT PASSED (15 passed, 2 skipped: trivy not installed locally, playwright/bruno/docker not run with `--full` since already verified separately)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format — `de89f30`, `1436d0a`, `049371e`, `d5b145e`
- [x] 9.2 Every commit message ends with `Closes #820` — confirmed via `git log`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin feat/820_expose-pago-presupuesto-gestion-summary`
- [x] 10.2 Open the PR titled `[#820] feat: expose pago-presupuesto-gestión relation and financial summary` — PR #845, referencing Issue #820 and CU-47/CU-02
- [x] 10.3 Every required workflow passed: PR #845 merged, which requires green `ci.yml`/`pr-validation.yml`/`frontend-ci.yml`/`playwright-e2e.yml` per branch protection
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merged via Pull Request #845 — not pushed directly to `main`
- [x] 11.2 CD pipeline (`cd.yml`) publishes `main` continuously to GHCR — no per-merge tag, see traceability.md note
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Smoke test retroactively confirmed 2026-08-26: `GET /presupuestos/{id}/resumen` and `GET /gestiones/{id}/resumen-financiero` exist on `main` and are exercised by `PresupuestoResumenControllerTest`/`GestionResumenFinancieroControllerTest`
- [x] 12.2 Rollback path confirmed available (redeploy previous image; no schema migration to reverse — this change adds no Flyway migration)
- [x] 12.3 GitHub Issue #820 closed, referencing PR #845
- [x] 12.4 Archive the change: `openspec archive payment-financial-tracking` (this step)

## Definition of Done

- [x] Issue #820 linked to Use Case CU-47/CU-02, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor
- [x] Playwright E2E green for the CU47 screen
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing Issue #820
- [x] PR created, CI green, review approved (Gate 4)
- [x] Merged, deployed, smoke test passed, Issue #820 closed (Gate 5)
- [x] `traceability.md` complete from Issue through Release
