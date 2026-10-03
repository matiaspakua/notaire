> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1198 exists, labeled, linked to CU13
- [x] 1.2 Use Case documentation exists (CU13)
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (behaviour fix)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b fix/1198_estado_actual_tie_break`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh fix-1198-estado-actual-tie-break`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: identical dates, distinct dates
- [x] 3.2 Write the failing tie test
- [x] 3.3 Observe it fail on current code
- [x] 3.4 Confirm every `#### Scenario:` maps to a test

## 4. Implementación

- [x] 4.1 Add the id tie-break to the comparator
- [x] 4.2 Tie test green

## 5. Actualizar tests existentes

- [x] 5.1 The #806 tests keep their assertions and pass
- [x] 5.2 No docs reference the old behaviour
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

- [x] 8.1 CHANGELOG Fixed entry
- [x] 8.2 CU13 ID table if present
- [x] 8.3 Confirm no information was duplicated
- [ ] 8.4 `bash scripts/preflight.sh`

## 9. Commits atómicos

- [ ] 9.1 Separate commits: spec, red test, fix, docs
- [ ] 9.2 Only the final commit may carry `Closes #1198`; others `Refs #1198`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin fix/1198_estado_actual_tie_break`
- [ ] 10.3 Open PR `[#1198] fix(api): deterministic estado-actual on tied history dates`
- [ ] 10.4 Wait for all required workflows to pass
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: create then update a gestion, `estado-actual` shows the new status
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1198 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1198-estado-actual-tie-break`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing test observed (Gate 2)
- [ ] Implementation passes tests and required CI (Gate 3–4)
- [ ] Coverage gate satisfied
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
