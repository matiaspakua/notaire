> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [ ] 1.1 GitHub Issue #796 exists, labeled (`enhancement,BACKEND,FRONTEND,CASO-DE-USO,priority:medium`), and linked to CU15 (#168)
- [ ] 1.2 Use Case documentation exists (`docs/100-business/102-use-cases/CU15 – Procesar pago.md`) — paso 5 will be updated in group 8 to list saldo pendiente among the displayed presupuesto data
- [ ] 1.3 Acceptance Criteria defined as scenarios in `specs/pago-presupuesto-picker-saldo/spec.md` (6 scenarios, done)
- [ ] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` (frontend only; no backend/schema/config/dependency changes)
- [ ] 1.5 ADR — not required; `proposal.md` — Architecture review confirms this follows the existing `<Select>`-fed-by-hook pattern already used in `gestiones/page.tsx`
- [ ] 1.6 Move Issue #796 to IN PROGRESS: `gh issue edit 796 --add-label "in-progress"`

## 2. Crear branch

- [ ] 2.1 `git checkout main && git pull origin main`
- [ ] 2.2 `git checkout -b feat/796_pago-presupuesto-picker-saldo`
- [ ] 2.3 Record the branch name in `traceability.md` (already pre-filled; confirm it matches the created branch)

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases from `specs/pago-presupuesto-picker-saldo/spec.md`: picker lists presupuestos by número + cliente, selecting one sets the payment target, empty-picker state, saldo pendiente shown on selection, saldo pendiente updates on re-selection, saldo pendiente fetch failure shows an unavailable state
- [ ] 3.2 `frontend/src/tests/unit/pagos-page.test.tsx` (new file, unit): add cases for the empty-picker state and the saldo pendiente error state (`usePresupuestoResumen` returning an error)
- [ ] 3.3 `frontend/tests/e2e/cu15-pagos.spec.ts` (Playwright): add/adjust cases for selecting a presupuesto from the picker, confirming it becomes the payment target, and the saldo pendiente displaying and updating on selection change
- [ ] 3.4 Run `cd frontend && npx vitest run pagos-page` and **observe the new unit tests fail** (picker and saldo display don't exist yet)
- [ ] 3.5 Run `cd frontend && npx playwright test cu15-pagos` and **observe the updated/new E2E cases fail** (numeric ID input still in place)
- [ ] 3.6 Confirm every `#### Scenario:` in the delta spec maps to at least one test (see design.md — Testing Strategy table)

## 4. Implementación

- [ ] 4.1 In `frontend/src/app/dashboard/pagos/page.tsx`, replace the "Presupuesto ID" `<Input type="number">` `FormField` with a `<Select>` fed by `usePresupuestos()`, listing each presupuesto as `Presupuesto #{idPresupuesto} — {fullName(persona)}` (`data-testid="select-presupuesto-pago"`, following the `select-presupuesto-gestion` convention in `gestiones/page.tsx`)
- [ ] 4.2 Show an empty state in the picker when `presupuestos` is empty (design.md — Testing Strategy: "No presupuestos available")
- [ ] 4.3 Wire the selected presupuesto's ID into `usePresupuestoResumen(selectedId)` and display its `saldoPendiente` (formatted via `formatCurrency`) in the form below the picker
- [ ] 4.4 Handle the `usePresupuestoResumen` error state by showing that the saldo pendiente is unavailable, without blocking re-selection (design.md — Testing Strategy: "Saldo pendiente fails to load")
- [ ] 4.5 Confirm the saldo pendiente display clears/updates correctly when the picker selection changes (re-fetch keyed by the new `idPresupuesto`)
- [ ] 4.6 Remove the now-unused numeric `idPresupuesto` input state/validation from the payment form

## 5. Actualizar tests existentes

- [ ] 5.1 Existing tests affected (design.md — Regression Strategy): `frontend/tests/e2e/cu15-pagos.spec.ts` cases that create/edit a payment via the removed numeric ID input
- [ ] 5.2 Update those cases to drive the new `<Select>` picker instead of typing an ID
- [ ] 5.3 No tests are made obsolete beyond the input-driving steps updated in 5.2

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — confirm no regression (this change does not touch `backend-api`)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — unaffected, confirm floor still holds
- [ ] 6.3 `mvn verify -pl backend-api` — unaffected, confirm still green
- [ ] 6.4 `bash testing/scripts/test.sh` — HTTP/Bruno API suite, confirm no endpoint regressions
- [ ] 6.5 `cd frontend && npx vitest run` — full frontend unit suite
- [ ] 6.6 No `@Disabled`/skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [ ] 7.1 Update `frontend/tests/e2e/cu15-pagos.spec.ts` per design.md — Playwright Strategy
- [ ] 7.2 `cd frontend && npx playwright test cu15-pagos` — all green
- [ ] 7.3 Verify the payment form's picker and saldo display at 320px, 768px and 1024px
- [ ] 7.4 n/a — has UI surface, see 7.1-7.3

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update `docs/100-business/102-use-cases/CU15 – Procesar pago.md` — paso 5: include saldo pendiente among the data shown for the selected presupuesto
- [ ] 8.2 No OpenAPI/Swagger changes — no endpoint changed
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — "Pagos: el formulario de cobro permite elegir el presupuesto de una lista y muestra su saldo pendiente antes de confirmar el pago"
- [ ] 8.4 No documents to archive — this is an additive UI change, not a superseding rewrite
- [ ] 8.5 Confirm CU15 doc and CHANGELOG entry don't duplicate information already in proposal.md/spec.md (those remain change-scoped, not permanent)
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format (e.g. picker; saldo display; error state; E2E updates; docs)
- [ ] 9.2 Every commit message ends with `Closes #796`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin feat/796_pago-presupuesto-picker-saldo`
- [ ] 10.2 Open the PR titled `[#796] feat: pago-presupuesto-picker-saldo`, referencing Issue #796 and CU15
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on the target environment: open the payment form, confirm the picker lists real presupuestos and selecting one shows its current saldo pendiente (design.md — Deployment Strategy)
- [ ] 12.2 Verify the rollback path described in design.md — Rollback Strategy is still available (code-only revert, no data migration)
- [ ] 12.3 Close Issue #796, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive pago-presupuesto-picker-saldo`

## Definition of Done

- [ ] Issue #796 linked to CU15, with Acceptance Criteria
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
