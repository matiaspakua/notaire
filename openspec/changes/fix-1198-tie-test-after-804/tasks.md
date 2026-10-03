> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1198 exists, labeled, linked to CU13
- [x] 1.2 Use Case documentation exists (CU13)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (test-only hotfix)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/1198_tie_test_after_804`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh fix-1198-tie-test-after-804`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: tie with direct append; distinct dates
- [x] 3.2 The failing state already exists on `main` (1 failure of 11), verified on a clean worktree
- [x] 3.3 Rewritten test observed to fail without the production fix (3 of 3)
- [x] 3.4 Every `#### Scenario:` maps to a test

## 4. Implementación

- [x] 4.1 Rewrite the tie test to append the second row directly
- [x] 4.2 Test green with the fix, 3 of 3

## 5. Actualizar tests existentes

- [x] 5.1 The other 10 tests in the class are untouched and pass
- [x] 5.2 No docs reference the old test
- [x] 5.3 Nothing else to adapt

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api`
- [ ] 6.2 `mvn jacoco:check -pl backend-api`
- [ ] 6.3 `mvn verify -pl backend-api`
- [ ] 6.4 Bruno via `bash scripts/run_pipeline.sh`
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no spec edits
- [ ] 7.2 Required Playwright CI job must still pass on the PR
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 CHANGELOG: none needed (no user-visible change; #1198 entry stands)
- [x] 8.2 CU13 table: #1198 already listed
- [x] 8.3 Confirm no information was duplicated
- [ ] 8.4 `bash scripts/preflight.sh`

## 9. Commits atómicos

- [ ] 9.1 Separate commits: spec, test
- [ ] 9.2 Only the final commit may carry `Closes #1198`; others `Refs #1198`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin fix/1198_tie_test_after_804`
- [ ] 10.3 Open PR `[#1198] fix(test): adapt the estado-actual tie test to #804`
- [ ] 10.4 Wait for all required workflows to pass
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: CI and CD green on the merge commit; `estado-actual` after create then transition
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1198 referencing the PRs
- [ ] 12.4 Archive the change: `openspec archive fix-1198-tie-test-after-804`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [x] Test cases designed; failing state observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Coverage gate satisfied
- [ ] Playwright n/a (no UI) but required CI jobs green
- [x] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
