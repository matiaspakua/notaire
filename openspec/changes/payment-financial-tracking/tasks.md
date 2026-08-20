> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #820 exists, labeled, and linked to Use Case CU-47 / CU-02
- [ ] 1.2 Use Case documentation (`CU47 – Consultar Pago.md`) exists and is accurate — confirm step 6's fields match the new endpoint's payload
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/pago-presupuesto-gestion-summary/spec.md`
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [ ] 1.5 No ADR required — follows existing layering (see design.md — Architecture review)
- [ ] 1.6 Move the Issue to IN PROGRESS: `gh issue edit 820 --add-label "in-progress"`

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/820_expose-pago-presupuesto-gestion-summary`
- [ ] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: payment response includes presupuesto (get by id, list by presupuesto, create); presupuesto summary with zero/one/many payments and unknown id; gestión summary with one/multiple/no-payment trámites
- [ ] 3.2 Write unit tests: `PagoControllerTest` (presupuesto id on responses), `PresupuestoResumenServiceTest`, `GestionResumenFinancieroServiceTest`
- [ ] 3.3 Write integration tests: `PresupuestoResumenControllerTest`, `GestionResumenFinancieroControllerTest` (H2/PostgreSQL, per `ApiH2IntegrationTest`/`ApiIntegrationTest` conventions)
- [ ] 3.4 Run them and **observe them fail** — `mvn test -pl backend-api -Dtest=PagoControllerTest,PresupuestoResumenServiceTest,GestionResumenFinancieroServiceTest,PresupuestoResumenControllerTest,GestionResumenFinancieroControllerTest`
- [ ] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test (cross-check against `traceability.md` — Requirement coverage)

## 4. Implementación

- [ ] 4.1 Add `DtoPagoResponse` (idPago, idPresupuesto, monto, fecha, metodoPago, observaciones) and map it in `PagoController`'s `getAll`, `getById`, `getByPresupuesto`, create/edit endpoints, replacing the direct `Pago` entity return
- [ ] 4.2 Add `PresupuestoResumenService` (new or extend `PagoService`) that assembles gestión número, encabezado, presupuesto número, total, saldo (reusing `PagoService.calcularSaldoPendiente`) and the list of `DtoPagoResponse` for a presupuesto
- [ ] 4.3 Add `GET /api/v1/presupuestos/{id}/resumen` in the appropriate controller, returning 404 for an unknown presupuesto
- [ ] 4.4 Add `GestionResumenFinancieroService` (or extend `GestionArchiveDebtService`) that sums total presupuestado, total cobrado and saldo across a gestión's linked presupuestos, reusing `calcularSaldoPendiente` for the saldo side
- [ ] 4.5 Add `GET /api/v1/gestiones/{id}/resumen-financiero` in `GestionController`
- [ ] 4.6 Document both new endpoints with `@Operation`/`@ApiResponse` (OpenAPI/Swagger)
- [ ] 4.7 Frontend: add/extend the CU47 consulta-de-pagos screen to call `/presupuestos/{id}/resumen` and display total, saldo and payment list without extra navigation, using `FormContainer`/`FormSection` from `@/theme/form-patterns`

## 5. Actualizar tests existentes

- [ ] 5.1 Identify existing tests affected: any `PagoControllerTest`/`PagoIntegrationTest` cases asserting the current `List<Pago>`/`Pago` response shape will need updating to the new `DtoPagoResponse` shape
- [ ] 5.2 Update them without weakening assertions — confirm they now also assert `idPresupuesto` is present
- [ ] 5.3 No test removal expected; note here if any becomes genuinely obsolete

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite (add requests for the two new endpoints)
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Add/update the CU47 consulta-de-pagos E2E spec under `frontend/tests/` covering: presupuesto with payments, presupuesto with none, gestión with multiple presupuestos
- [ ] 7.2 `cd frontend && npx playwright test` — all green
- [ ] 7.3 Verify the CU47 screen at 320px, 768px and 1024px
- [ ] 7.4 n/a does not apply — this change has a UI surface (CU47 screen)

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` per proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations for the two new endpoints; verify in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) with the resumen-financiero entry
- [ ] 8.4 No documents to archive for this change
- [ ] 8.5 Confirm no information duplicated — CU47 doc remains the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix`

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #820`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/820_expose-pago-presupuesto-gestion-summary`
- [ ] 10.2 Open the PR titled `[#820] feat: expose pago-presupuesto-gestión relation and financial summary`, referencing Issue #820 and CU-47/CU-02
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment: `GET /presupuestos/{id}/resumen` and `GET /gestiones/{id}/resumen-financiero` against a known record, plus the CU47 screen flow
- [ ] 12.2 Verify the rollback path (redeploy previous image; no schema migration to reverse — this change adds no Flyway migration)
- [ ] 12.3 Close GitHub Issue #820, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive payment-financial-tracking`

## Definition of Done

- [ ] Issue #820 linked to Use Case CU-47/CU-02, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for the CU47 screen
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing Issue #820
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue #820 closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
