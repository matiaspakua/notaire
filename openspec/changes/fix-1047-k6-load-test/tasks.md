> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`) — #1047 OPEN; CU74 + CU76
- [x] 1.2 Use Case documentation exists and is accurate — CU74/CU76 exist; update AC/docs at implement for restored k6
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (restore CI asset)
- [x] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1047 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — **after #1048 merges** (queue: #1057 → #1048 → #1047)
- [x] 2.2 `git checkout -b cursor/fix-1047-k6-load-test-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1047/` into `openspec/changes/fix-1047-k6-load-test/` and run `bash scripts/validate-sdlc-plan.sh fix-1047-k6-load-test`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: script exists; English login fields; stages + SLO thresholds; endpoint+Bearer coverage; summary artifact; workflow schedule/live wiring; asset suite green
- [x] 3.2 Extend `scripts/test_performance_test_assets.py` with failing asserts for English `name`/`password`, p95≤2000 / `rate<0.01`, and `handleSummary`/`summary.json` (existing path asserts already fail while file missing)
- [x] 3.3 Keep/adjust workflow asserts so schedule + k6 path + health wait remain required; add artifact-path expectation if missing
- [x] 3.4 Run them and **observe them fail** — `python3 scripts/test_performance_test_assets.py`
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test

## 4. Implementación

- [x] 4.1 Recreate `performance-test/k6/` and write `load-test.js` for English login + Bearer JWT
- [x] 4.2 Cover GET `/api/v1/gestiones`, `/api/v1/presupuestos`, `/api/v1/tramites` with checks
- [x] 4.3 Set `stages` (≈#594 profile) and CU74-tied `thresholds` (`http_req_duration` p95≤2000ms, `http_req_failed` rate&lt;0.01)
- [x] 4.4 Implement `handleSummary` (or equivalent) writing `summary.json` for upload-artifact
- [x] 4.5 Adjust `.github/workflows/performance-test.yml` only if needed (artifact path / `if-no-files-found`); do not add `pull_request` trigger
- [x] 4.6 Make asset unittest suite pass without weakening SLO or English-API asserts

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing asset/workflow tests affected (`test_performance_test_assets.py`)
- [x] 5.2 Update assertions for English DTO + summary output without weakening gates
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason — none expected (Spanish field asserts should not be reintroduced)

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — sanity only (no backend delta expected)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a if no backend code
- [x] 6.3 `mvn verify -pl backend-api` — n/a if no backend code; CI verifies
- [x] 6.4 `bash integration-test/scripts/test.sh` — n/a (no API change); Bruno covered by heavy CI
- [x] 6.5 No `@Disabled` or skipped tests without documented, approved justification

## 7. Ejecutar Playwright

- [x] 7.1 No new E2E required (no UI delta)
- [ ] 7.2 PR must pass full `playwright-e2e.yml` via heavy CI gate
- [x] 7.3 Viewport checks n/a
- [x] 7.4 Mark product Playwright scenarios n/a for this change; do not skip the PR Playwright job

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [x] 8.2 Update OpenAPI/Swagger — n/a
- [x] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) for restored k6 load-test (#1047)
- [x] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [x] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [x] 8.6 `bash scripts/preflight.sh` (or applicable subset) — keep local/CI mapping honest if perf assets mentioned

## 9. Commits atómicos

- [x] 9.1 Commit in small, self-contained units, Conventional Commits format
- [x] 9.2 Every commit message ends with `Closes #1047`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1047-k6-load-test-69d3`
- [ ] 10.2 Open the PR titled `[#1047] test(perf): restore k6 load-test script`, referencing Issue, CU74, CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete; merge only on heavy-CI gate exit 0
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR (if app image changed; asset/workflow-only may be n/a)
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `workflow_dispatch` on `performance-test.yml` — k6 step green; artifact `k6-load-test-results` contains `summary.json`
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive fix-1047-k6-load-test`

## Definition of Done

- [x] Issue linked to a Use Case / RNF, with Acceptance Criteria (CU74 + CU76)
- [x] Specification written and reviewed (Gate 1 draft)
- [x] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor (n/a backend delta)
- [ ] Playwright E2E green for UI changes (n/a product UI; heavy gate still required)
- [x] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [x] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
