> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1057 OPEN; RNF a11y + CU76 (CU gap noted in proposal)
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update AC at implement for icon naming
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1057 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1148 merges** (serialize after #1147 / #1148)
- [x] 2.2 `git checkout -b cursor/fix-1057-a11y-icon-names-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1057/` into `openspec/changes/fix-1057-a11y-icon-names/` and run `bash scripts/validate-sdlc-plan.sh fix-1057-a11y-icon-names`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (named edit/delete/resumen; lint fail/pass; CU21 unskip)
- [x] 3.2 Write failing Playwright assertions (`getByRole('button', { name })`) for inventoried pages before adding labels
- [x] 3.3 Add/adjust lint fixture or document failing lint on a known-bad icon Button — observe fail
- [x] 3.4 Run them and **observe them fail** — `cd frontend && npx playwright test TS-0096` (id confirm); `npm run lint`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Re-scan dashboard for icon-only Buttons missing `aria-label`/`title`/sr-only; refresh inventory if count ≠ 17
- [x] 4.2 Add `aria-label={tc("edit")}` / `aria-label={tc("delete")}` on usuarios, roles, escrituras, pagos, personas
- [x] 4.3 Add translated `aria-label` on presupuestos Receipt (resumen) action; add i18n key if missing
- [x] 4.4 Add explicit `aria-label` on administracion conceptos / documentos / tramites edit+delete (do not rely on img alt alone); avoid duplicate SR names if needed (`alt=""` + aria-hidden on decorative icon)
- [x] 4.5 Static unit inventory gate (`icon-button-aria-labels.test.ts`) asserts `aria-label=` on all 17 call sites (stock jsx-a11y unreliable on Lucide children — documented)
- [x] 4.6 Keep design-system tokens / Button variants unchanged — attributes and lint only

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected (TS-0016 skipped CU21; any brittle button nth locators)
- [x] 5.2 Unskip TS-0016 CU21-GW01 (and GW02 if still valid) without weakening assertions
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — sanity only (no backend delta expected)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a if no backend code
- [ ] 6.3 `mvn verify -pl backend-api` — n/a if no backend code; CI verifies
- [ ] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change)
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 Add `frontend/tests/e2e/TS-0096-icon-button-accessible-names.spec.ts` (confirm free TS id vs TS-0095 from #1054); update `E2E-TEST-MAPPING.md`
- [ ] 7.2 `cd frontend && npx playwright test` — all green (at least TS-0096 + TS-0016)
- [ ] 7.3 Verify affected screens at 320px, 768px and 1024px (layout unchanged; controls still reachable)
- [ ] 7.4 n/a only if no UI — this change HAS UI surface

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for user-visible a11y fix
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1057`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1057-a11y-icon-names-69d3`
- [ ] 10.2 Open the PR titled `[#1057] fix(frontend): accessible names for icon-only buttons`, referencing Issue and CU76 / RNF a11y
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: Usuarios (and one admin NotaireIcon page) — edit/delete reachable by accessible name
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1057-a11y-icon-names`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76 + RNF a11y; CU gap noted)
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor (n/a backend delta)
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
