> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #585 exists, labeled, linked to CU76; scope widened by comment
- [x] 1.2 Use Case documentation exists (CU76); #585 already in its ID table
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (deletion only)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b chore/585_remove_orphans`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh chore-585-remove-orphans`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: tree gone, no orphan script, removed scripts gone, no live reference
- [x] 3.2 Write the failing guards in `test_repo_hygiene.py` and `test_testing_standalone.py`
- [x] 3.3 Observe them fail before deleting anything
- [x] 3.4 Confirm every `#### Scenario:` maps to a test

## 4. Implementación

- [x] 4.1 `git rm -r deprecated-src.old` (own commit)
- [x] 4.2 `git rm` the nine orphaned cURL scripts (own commit)
- [x] 4.3 Remove the unused `COMPOSE_FILES` constant
- [x] 4.4 Guards green

## 5. Actualizar tests existentes

- [x] 5.1 Existing guards pass, assertions unchanged
- [x] 5.2 Fix docs that described the removed scripts
- [x] 5.3 No dead references remain

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java touched); `mvn -q -pl backend-api -am validate` proves the build ignores the removed tree
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 Bruno and cURL suites via `bash scripts/run_pipeline.sh`
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no spec edits
- [ ] 7.2 Required Playwright CI job must still pass on the PR
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 api-test README, 303-testing README, testing DEFINITION
- [x] 8.2 CHANGELOG entry; confirm CU76 table
- [x] 8.3 Confirm no information was duplicated
- [x] 8.3a Complete #799's hand-fold in `persona-validacion-duplicados` (3 scenarios + 1 sentence) and re-validate the spec strictly
- [ ] 8.4 `bash scripts/preflight.sh` without bypass

## 9. Commits atómicos

- [ ] 9.1 Separate commits: spec, red guards, tree deletion, scripts deletion, docs
- [ ] 9.2 Only the final commit may carry `Closes #585`; others `Refs #585`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin chore/585_remove_orphans` without `PREFLIGHT_SKIP`
- [ ] 10.3 Open PR `[#585] chore(repo): remove orphaned files`
- [ ] 10.4 Wait for all required workflows to pass
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `run.sh integration` and `run.sh database` green on merged `main`
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #585 referencing the PR
- [ ] 12.4 Archive the change: `openspec archive chore-585-remove-orphans`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guards observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
