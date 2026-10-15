> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `workspace/sdlc/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1753 exists and is linked to CU76
- [x] 1.2 Use Case CU76 exists
- [x] 1.3 Acceptance Criteria defined as scenarios in `specs/`
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 No new ADR; ADR-028 applies
- [ ] 1.6 Move the Issue to IN PROGRESS — label write silently dropped for fleet token (#1551)

## 2. Crear branch

- [x] 2.1 Branch from `origin/main` (`e0236cd15`)
- [x] 2.2 `git checkout -b 1753/fix/release-please-version-files`
- [x] 2.3 Record the branch name in `traceability.md`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate: POM XPaths, OpenAPI sync (only info.version, idempotent, SemVer), workflow step, exemption positive and two negatives
- [x] 3.2 Write `test_release_please_version_files.py` and three `test_pr_checks.py` cases
- [x] 3.3 n/a — no Java/HTTP integration tests
- [x] 3.4 Observed fail: 6 failures in commit `5a13ff939`
- [x] 3.5 Every `#### Scenario:` maps to a test

## 4. Implementación

- [x] 4.1 `local-name()` XPaths in `release-please-config.json`
- [x] 4.2 `workspace/release/sync-openapi-version.py` + release-please.yml steps
- [x] 4.3 Release-please exemption in `check-sdlc-exception.sh`; `PR_HEAD_REF` in `sdlc-process.yml`

## 5. Actualizar tests existentes

- [x] 5.1 `run()` in `test_pr_checks.py` defaults `PR_HEAD_REF` to empty
- [x] 5.2 No assertions weakened
- [x] 5.3 No tests removed

## 6. Ejecutar regresión

- [x] 6.1 Release + PR-check suites: 91 tests OK; `quick.sh` OK
- [x] 6.2 n/a — no Java change
- [x] 6.3 n/a — JaCoCo unchanged
- [x] 6.4 n/a — SpotBugs/Checkstyle unchanged
- [x] 6.5 n/a — no endpoint change
- [x] 6.6 No `@Disabled` tests added

## 7. Ejecutar Playwright

- [x] 7.1 n/a — no UI surface
- [x] 7.2 n/a — no Playwright spec to add
- [x] 7.3 n/a — no layout change
- [x] 7.4 Record "n/a — no UI surface": workflow + scripts only

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 RELEASE.md as listed in proposal.md
- [x] 8.2 n/a — no OpenAPI contract change (sync script only)
- [x] 8.3 `CHANGELOG.md` `[Unreleased]` Bug Fixes entry for #1753
- [x] 8.4 No documents to archive
- [x] 8.5 No duplication — RELEASE.md is the source
- [x] 8.6 Process gates via `quick.sh`

## 9. Commits atómicos

- [x] 9.1 Conventional Commits; tests first, then implementation
- [x] 9.2 Commits reference #1753
- [x] 9.3 No secrets, no unrelated changes
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 Push `1753/fix/release-please-version-files`
- [x] 10.2 Open PR #1754 `[#1753] fix(release): release-please bumps POMs/OpenAPI; release PR exempt`
- [ ] 10.3 Wait for required workflows
- [ ] 10.4 Gate 4 — CI green, no conflicts, docs complete
- [x] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only (Owner)
- [ ] 11.2 n/a — this change does not publish images
- [ ] 11.3 Record merge SHA in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Next release-please PR is green with no manual commit and no `sdlc-exception` label
- [ ] 12.2 Rollback = revert of this PR
- [ ] 12.3 Close #1753 after the smoke; archive this OpenSpec change

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes (n/a — no UI)
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
