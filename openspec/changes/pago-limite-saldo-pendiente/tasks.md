> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #848 exists, labeled (`enhancement,BACKEND,FRONTEND,CASO-DE-USO,priority:medium`), and linked to CU15 / RF-18.2
- [ ] 1.2 Use Case documentation exists (`docs/100-business/102-use-cases/CU15 – Procesar pago.md`) — paso 11 will be updated in group 8 to reflect the new rejection behavior
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/pago-limite-saldo-pendiente/spec.md` (5 scenarios, done)
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` (backend-api, frontend; no schema/config/dependency changes)
- [ ] 1.5 ADR — not required; `proposal.md` — Architecture review confirms this follows the existing service-layer-validates / controller-translates-status pattern
- [ ] 1.6 Move Issue #848 to IN PROGRESS: `gh issue edit 848 --add-label "in-progress"`

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b fix/848_reject-payments-exceeding-saldo`
- [ ] 2.3 Record the branch name in `traceability.md` (already pre-filled; confirm it matches the created branch)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from `specs/pago-limite-saldo-pendiente/spec.md`: within-saldo accepted, exact-match accepted, exceeding-saldo rejected (409), saldo-already-reduced-by-prior-payments respected, frontend shows specific message on 409
- [ ] 3.2 `PagoServiceTest` (unit): add cases for `procesarPago` — monto < saldoPendiente accepted; monto == saldoPendiente accepted; monto > saldoPendiente throws `SaldoPendienteExcedidoException`; saldo already reduced by a prior payment is respected (assert via `calcularSaldoPendiente` reuse)
- [ ] 3.3 `PagoControllerTest` (unit): add cases — `POST /pagos` and `POST /pagos/params` return HTTP 409 when the service throws `SaldoPendienteExcedidoException`
- [ ] 3.4 Integration test (`PagoIntegrationTest` or `PagoServiceIntegrationTest`, whichever exercises `procesarPago` end-to-end against PostgreSQL): add a case asserting `POST /api/v1/pagos` with `monto` > saldo pendiente returns 409 and the payment is NOT persisted
- [ ] 3.5 Run `mvn test -pl backend-api -Dtest=PagoServiceTest,PagoControllerTest` and **observe them fail** (class `SaldoPendienteExcedidoException` and the new controller `catch` do not exist yet)
- [ ] 3.6 Confirm every `#### Scenario:` in the delta spec maps to at least one test (see design.md — Testing Strategy table)

## 4. Implementación

- [ ] 4.1 Create `SaldoPendienteExcedidoException extends RuntimeException` (package `com.licensis.notaire.exception` or `com.licensis.notaire.service`, per design.md — Decisions: it is intentionally NOT a `NotaireException` subclass)
- [ ] 4.2 In `PagoService.procesarPago`, after the existing `monto <= 0` check, call `calcularSaldoPendiente(idPresupuesto)` and throw `SaldoPendienteExcedidoException` when `monto > saldoPendiente`; remove the now-redundant inline `totalPresupuesto`/`totalPagado`/`saldoPendiente` computation used only for logging, replacing it with the reused `calcularSaldoPendiente` result
- [ ] 4.3 In `PagoController.procesarPago` (`POST /pagos`), add `catch (SaldoPendienteExcedidoException e)` before the generic `catch (Exception e)`, returning `ResponseEntity.status(HttpStatus.CONFLICT).build()`
- [ ] 4.4 In `PagoController.procesarPagoParams` (`POST /pagos/params`), add the same `catch (SaldoPendienteExcedidoException e)` → 409 branch
- [ ] 4.5 In `frontend/src/app/dashboard/pagos/page.tsx`, detect `error instanceof ApiError && error.status === 409` in the payment-save error handler and show a specific translated message ("el monto excede el saldo pendiente") instead of the generic `errorSave` toast
- [ ] 4.6 Add the new message key(s) to the frontend i18n/translation source used by `pagos/page.tsx`

## 5. Actualizar tests existentes

- [ ] 5.1 Existing tests affected (design.md — Regression Strategy): `PagoServiceTest` and `PagoControllerTest` cases covering the accepted-payment and `monto <= 0` paths for `procesarPago`/`procesarPagoParams`
- [ ] 5.2 Confirm they still pass unchanged — this change only adds a new rejection branch above the existing floor check, it does not alter `monto <= 0` behavior or the accepted-payment response shape
- [ ] 5.3 No tests are made obsolete by this change

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates (Checkstyle, SpotBugs)
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Update `frontend/tests/e2e/cu15-pagos.spec.ts`: add a case that submits `monto` greater than the presupuesto's saldo pendiente and asserts the specific rejection message is shown (not the generic `errorSave` toast)
- [ ] 7.2 `cd frontend && npx playwright test cu15-pagos` — all green
- [ ] 7.3 Verify the payment form's new error state at 320px, 768px and 1024px
- [ ] 7.4 n/a — has UI surface, see 7.1-7.3

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU15 – Procesar pago.md` — paso 11: the system can reject the payment when `monto` exceeds saldo pendiente, not only "calculate and show" it (proposal.md — Documentation Impact)
- [ ] 8.2 Update `@ApiResponses` on `PagoController.procesarPago`/`procesarPagoParams` if the description text needs correcting now that `409` is actually reachable; verify both endpoints in Swagger UI
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — "Pagos: se rechaza un pago cuyo monto excede el saldo pendiente del presupuesto"
- [ ] 8.4 No documents to archive — this is a bug-fix scoped change, not a superseding rewrite
- [ ] 8.5 Confirm CU15 doc and CHANGELOG entry don't duplicate information already in proposal.md/spec.md (those remain change-scoped, not permanent)
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. backend validation + exception; controller mapping; frontend message; docs)
- [ ] 9.2 Every commit message ends with `Closes #848`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/848_reject-payments-exceeding-saldo`
- [ ] 10.2 Open the PR titled `[#848] fix: reject payments exceeding saldo pendiente (CU15)`, referencing Issue #848 and CU15/RF-18.2
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on the target environment: `POST /api/v1/pagos` with `monto` > saldo pendiente returns 409; `monto` within saldo still returns 201 (design.md — Deployment Strategy)
- [ ] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available (code-only revert, no data migration)
- [ ] 12.3 Close Issue #848, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive pago-limite-saldo-pendiente`

## Definition of Done

- [ ] Issue #848 linked to CU15 / RF-18.2, with Acceptance Criteria
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
