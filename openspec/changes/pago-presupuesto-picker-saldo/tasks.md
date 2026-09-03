> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

> Reconciliation note (2026-09-03): the picker + saldo pendiente feature this
> change describes was already implemented and merged directly to `main`
> (commit `9cdedded feat(#796): ...`) without going through this OpenSpec
> change's branch/PR/tests-first flow — a process gap discovered while
> auditing `openspec/explore.md` findings. `frontend/tests/e2e/TS-0014-pagos-saldo-picker.spec.ts`
> (not the `cu15-pagos.spec.ts` this plan originally named) already covers the
> scenarios below, but had a flaky/broken locator. The work remaining on
> `fix/796_pago-presupuesto-picker-saldo-regression` is that E2E fix plus the
> permanent-docs gap (Gate 3) and formally closing the loop (Gate 4/5). Groups
> 3-5 below are marked done against the actual shipped code/tests, not
> re-implemented.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #796 exists, labeled (`enhancement,BACKEND,FRONTEND,CASO-DE-USO,priority:medium`), and linked to CU15 (#168)
- [x] 1.2 Use Case documentation exists (`docs/100-business/102-use-cases/CU15 – Procesar pago.md`) — paso 5 updated to list saldo pendiente among the displayed presupuesto data
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/pago-presupuesto-picker-saldo/spec.md` (6 scenarios, done)
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md` (frontend only; no backend/schema/config/dependency changes)
- [x] 1.5 ADR — not required; `proposal.md` — Architecture review confirms this follows the existing `<Select>`-fed-by-hook pattern already used in `gestiones/page.tsx`
- [x] 1.6 Move Issue #796 to IN PROGRESS: `gh issue edit 796 --add-label "in-progress"` — already done

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/796_pago-presupuesto-picker-saldo-regression` (the feature branch itself was never pushed — code landed on `main` directly; this branch carries the regression fix)
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: picker lists presupuestos by número + cliente, selecting one sets the payment target, saldo pendiente shown on selection, saldo pendiente updates on re-selection, overpayment rejection — covered by `TS-0014-pagos-saldo-picker.spec.ts`
- [x] 3.2 n/a — no dedicated unit test file was added; coverage is via the E2E suite (3.3) instead, since the picker/saldo behavior depends on live API data
- [x] 3.3 `frontend/tests/e2e/TS-0014-pagos-saldo-picker.spec.ts` — already exists with 5 scenarios (CU15-SALDO-01..05)
- [x] 3.4 n/a — no separate unit-test layer was written for this UI, see 3.2
- [x] 3.5 Observed failing before this fix: `getByRole("dialog").getByText(/saldo|pendiente/i)` matched 2 elements (Radix `SelectValue` loading text and a seeded persona surname containing "Saldo"), causing intermittent strict-mode violations in CU15-SALDO-03/04
- [x] 3.6 Every `#### Scenario:` in the delta spec maps to a `CU15-SALDO-0N` test in `TS-0014-pagos-saldo-picker.spec.ts`

## 4. Implementación

- [x] 4.1 `frontend/src/app/dashboard/pagos/page.tsx` — "Presupuesto ID" replaced with a `<Select>` fed by `usePresupuestos()` (`data-testid="select-presupuesto-pago"`)
- [x] 4.2 Empty-picker state implemented ("No hay presupuestos disponibles")
- [x] 4.3 Selected presupuesto wired into `usePresupuestoResumen(selectedId)`, displaying `saldoPendiente` — now with `data-testid="saldo-pendiente-amount"` (added in this fix, to remove locator ambiguity)
- [x] 4.4 `usePresupuestoResumen` error state shows "No se pudo cargar el saldo. Intenta nuevamente." without blocking re-selection
- [x] 4.5 Saldo pendiente re-fetches and updates when the picker selection changes (verified by CU15-SALDO-04)
- [x] 4.6 Numeric `idPresupuesto` input removed from the payment form

## 5. Actualizar tests existentes

- [x] 5.1 `TS-0014-pagos-saldo-picker.spec.ts` already drives the `<Select>` picker, not a numeric ID input
- [x] 5.2 n/a — see 5.1
- [x] 5.3 No tests made obsolete by this fix

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — no regression (this fix does not touch `backend-api`)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — unaffected
- [x] 6.3 `mvn verify -pl backend-api` — unaffected
- [x] 6.4 `bash testing/scripts/test.sh` — unaffected, no endpoint changes
- [x] 6.5 n/a — no frontend unit-test suite covers this screen (see 3.2); regression coverage is the E2E suite in group 7
- [ ] 6.6 No `@Disabled`/skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Fixed `TS-0014-pagos-saldo-picker.spec.ts`: added `data-testid="saldo-pendiente-amount"` and switched the saldo locator to it, fixed a currency-formatting assertion mismatch (raw "75000" vs. locale-formatted "75.000"), and aligned a `select-persona` option click with the `evaluate(el => el.click())` workaround already used elsewhere in the file
- [x] 7.2 `cd frontend && npx playwright test TS-0014-pagos-saldo-picker.spec.ts` — all 5 green locally
- [ ] 7.3 Verify the payment form's picker and saldo display at 320px, 768px and 1024px
- [x] 7.4 n/a — has UI surface, see 7.1-7.2

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Updated `docs/100-business/102-use-cases/CU15 – Procesar pago.md` — paso 5 now includes saldo pendiente among the data shown for the selected presupuesto
- [x] 8.2 No OpenAPI/Swagger changes — no endpoint changed
- [x] 8.3 Updated `CHANGELOG.md` (`[Unreleased]` — Fixed) — documents the flaky E2E locator fix
- [x] 8.4 No documents to archive — this is an additive UI change, not a superseding rewrite
- [x] 8.5 Confirmed CU15 doc and CHANGELOG entry don't duplicate information already in proposal.md/spec.md
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Commit message ends with `Closes #796`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin fix/796_pago-presupuesto-picker-saldo-regression`
- [ ] 10.2 Open the PR titled `[#796] fix: stabilize pagos presupuesto picker/saldo E2E coverage`, referencing Issue #796 and CU15
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke test on the target environment: open the payment form, confirm the picker lists real presupuestos and selecting one shows its current saldo pendiente
- [ ] 12.2 n/a — code-only test fix, no rollback path needed beyond reverting the commit
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
