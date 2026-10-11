> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU76`) — #1511
- [x] 1.2 Use Case documentation exists — CU76
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR-006 will be updated (existing ADR)
- [x] 1.6 Move the Issue to IN PROGRESS

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b cursor/1511-testing-architecture-b585`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate scenarios from delta spec
- [x] 3.2 Write `testing/tests/test_testing_inventory.py` for inventory + naming + docs presence
- [x] 3.3 Observe tests fail before generator/renames/docs land
- [x] 3.4 Confirm every `#### Scenario:` maps to at least one test

## 4. Implementación

- [x] 4.1 Add `workspace/sdlc/generate-testing-inventory.py`
- [x] 4.2 Add `workspace/sdlc/validate-testing-inventory.py`
- [x] 4.3 Rename nine unprefixed E2E specs to `TS-0130`–`TS-0138`
- [x] 4.4 Generate and commit `docs/300-development/303-testing/INVENTORY.md`
- [x] 4.5 Rewrite `TEST-PLAN.md` as Master Test Plan
- [x] 4.6 Add `PROCESS-ARCHITECTURE.md`
- [x] 4.7 Update ADR-006, CONSTITUTION §7, README, coverage strategy, CU76
- [x] 4.8 Archive `LOGIN-TEST-REPORT.md`; add `test-cases/README.md`
- [x] 4.9 Wire inventory validation into `preflight.sh`
- [x] 4.10 Fix `backend-api/pom.xml` JaCoCo comment to match 80/65

## 5. Actualizar tests existentes

- [x] 5.1 Update active OpenSpec references to renamed Playwright filenames
- [x] 5.2 Ensure `test_e2e_reliability.py` still passes (hotspot list unchanged)
- [x] 5.3 No assertion weakening

## 6. Ejecutar regresión

- [x] 6.1 `python3 -m unittest discover -s testing/tests`
- [x] 6.2 `python3 workspace/sdlc/validate-testing-inventory.py`
- [x] 6.3 `mvn test -pl backend-api` — n/a product Java (pom comment only)
- [x] 6.4 `bash workspace/sdlc/validate-sdlc-plan.sh docs-1511-testing-architecture`
- [x] 6.5 No `@Disabled` or skipped tests without justification

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface; renames verified by inventory validator + unittest
- [x] 7.2 Record reason: process/docs/tooling change only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 OpenAPI/Swagger: n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`)
- [x] 8.4 Archive superseded snapshot report
- [x] 8.5 Confirm no duplicated pyramids — link to MTP/INVENTORY
- [x] 8.6 Run inventory validation + testing unittest discover

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1511` (or final commit/PR body)
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/1511-testing-architecture-b585`
- [ ] 10.2 Open the PR titled `[#1511] docs(test): unify Master Test Plan and testing architecture`
- [ ] 10.3 Wait for required workflows (path-scoped; expect docs/sdlc jobs)
- [ ] 10.4 Gate 4 — CI green, review, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only
- [ ] 11.2 CD publish: n/a for docs-only; confirm no runtime image coupling
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `python3 workspace/sdlc/validate-testing-inventory.py` on post-merge tip
- [ ] 12.2 Rollback path: revert PR
- [ ] 12.3 Close Issue #1511 via `Closes #1511`
- [ ] 12.4 Archive the change: `openspec archive docs-1511-testing-architecture`

## Definition of Done

- [ ] Issue linked to CU76, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Inventory validation + testing unittest green
- [ ] Coverage floors documented match enforcement
- [ ] Playwright: n/a UI; naming enforced
- [ ] Permanent documentation updated (MTP, process arch, ADR, Constitution)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, smoke passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
