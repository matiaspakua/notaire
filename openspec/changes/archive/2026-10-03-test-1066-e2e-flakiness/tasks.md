> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**: a plan that omits one
> is incomplete and `scripts/validate-sdlc-plan.sh` will reject it. Add
> change-specific work inside group 4; do not renumber the mandatory groups.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue exists, labeled, and linked to a Use Case (`CU-XX` / `RF-XX` / `RNF-XX`)
- [x] 1.2 Use Case documentation exists and is accurate — create or update it first if not
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR recorded under `docs/200-architecture/202-ADR/` if the change is architectural — n/a (test-infra; no architectural change)
- [ ] 1.6 Move the Issue to IN PROGRESS (`gh issue edit 1066 --add-label "in-progress"`) — defer until implement; label ACL often 403 for integration

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main` — after #1145 merges
- [x] 2.2 `git checkout -b cursor/fix-1066-e2e-flakiness-69d3`
- [x] 2.3 Record the branch name in `traceability.md`
- [x] 2.4 Copy this draft from `internal/openspec-1066/` into `openspec/changes/test-1066-e2e-flakiness/` and run `bash scripts/validate-sdlc-plan.sh test-1066-e2e-flakiness`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: happy path, edge cases, error paths (arrange-then-assert; arrange failure; no sleep; skip citations; retries config)
- [x] 3.2 Write / tighten failing assertions first: observe current TS-0021/22 skip-on-empty behavior; add arrange-failure must-throw expectations; document grep/lint expectation for `waitForTimeout` in named files
- [x] 3.3 Write the integration tests where applicable — n/a backend; Playwright is the primary level
- [x] 3.4 Run them and **observe them fail** — `cd frontend && npx playwright test TS-0021 TS-0022` (empty DB / no skip path) and confirm sleeps still present before removal
- [x] 3.5 Confirm every `#### Scenario:` in the delta spec maps to at least one test or mechanical check

## 4. Implementación

- [x] 4.1 Extend/use `setup/api-helpers.ts` so TS-0021 seeds a workflow (and editor entry) before graph tests; remove data-missing `test.skip()`
- [x] 4.2 Seed tipo-tramite (and workflow if needed) for TS-0022; remove empty-table `test.skip()`
- [x] 4.3 Replace `waitForTimeout` in TS-0040 and TS-0043 with web-first assertions; trim unnecessary `networkidle` in those files
- [x] 4.4 Annotate intentional skips in TS-0014/16/17/20 with open `#issue` citations (create tracking issues only if none exist)
- [x] 4.5 Set Playwright CI `retries` to `1`; retain trace/screenshot/video for flake triage; tighten global timeout with demo exceptions as needed
- [x] 4.6 Gate demo paced pauses (TS-0071/TS-0090) behind HEADED/SLOW_MO if they still use `waitForTimeout` for pacing

## 5. Actualizar tests existentes

- [x] 5.1 Identify existing tests affected by the change (see design.md — Regression Strategy)
- [x] 5.2 Update them without weakening assertions; document why any old skip/sleep expectation was wrong
- [x] 5.3 Remove tests made genuinely obsolete, stating the reason

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — unit + integration (sanity; no backend delta expected)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — coverage ratchet floor — n/a if no backend code
- [ ] 6.3 `mvn verify -pl backend-api` — all quality gates — n/a if no backend code; CI verifies
- [ ] 6.4 `bash integration-test/scripts/test.sh` — HTTP/Bruno API suite (n/a — no API change)
- [ ] 6.5 No `@Disabled` or skipped tests without documented, approved justification (cited `#issue` only)

## 7. Ejecutar Playwright

- [ ] 7.1 Run focused suites: `TS-0021`, `TS-0022`, `TS-0040`, `TS-0043` per design.md — Playwright Strategy; update `E2E-TEST-MAPPING.md`
- [ ] 7.2 `cd frontend && npx playwright test` — all green (with stack up)
- [ ] 7.3 Verify key assertions still hold at 320px, 768px and 1024px where those specs cover viewports — or n/a if untouched
- [ ] 7.4 n/a only if no UI — this change HAS E2E surface

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Update every permanent document listed in proposal.md — Documentation Impact
- [ ] 8.2 Update OpenAPI/Swagger annotations if endpoints changed, and verify in Swagger UI — n/a
- [ ] 8.3 Update `CHANGELOG.md` (`[Unreleased]`) — n/a not user-visible; note engineering-only if project requires
- [ ] 8.4 Archive superseded documents into `docs/000-archive/` — none expected
- [ ] 8.5 Confirm no information was duplicated — permanent docs remain the single source of truth
- [ ] 8.6 `bash scripts/preflight.sh --fix` — mirrors every CI gate

## 9. Commits atómicos

- [ ] 9.1 Commit in small, self-contained units, Conventional Commits format
- [ ] 9.2 Every commit message ends with `Closes #1066`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record the commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `git push -u origin cursor/fix-1066-e2e-flakiness-69d3`
- [ ] 10.2 Open the PR titled `[#1066] test(e2e): arrange data, web-first waits, reduce CI retries`, referencing Issue and CU76
- [ ] 10.3 Wait for every required workflow to pass: `ci.yml`, `pr-validation.yml`, `frontend-ci.yml`, `playwright-e2e.yml`
- [ ] 10.4 Gate 4 — CI green, code review approved, no merge conflicts, docs complete
- [ ] 10.5 Record the PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Merge via the Pull Request only — never push to `main`
- [ ] 11.2 Confirm the CD pipeline (`cd.yml`) published the image to GHCR — n/a if no product image change; confirm main CI Playwright green instead
- [ ] 11.3 Record the merge commit and release/tag in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Run the smoke test on the target environment — confirm CI Playwright with retries=1; TS-0021/22 do not skip for missing data
- [ ] 12.2 Verify the rollback path is still available as described in design.md
- [ ] 12.3 Close the GitHub Issue, referencing the PR
- [ ] 12.4 Archive the change: `openspec archive test-1066-e2e-flakiness`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1 draft)
- [ ] Tests designed and written first, observed failing (Gate 2)
- [ ] Full suite green: unit, integration, regression, E2E
- [ ] Coverage at or above the JaCoCo ratchet floor
- [ ] Playwright E2E green for UI changes
- [ ] Permanent documentation updated, consistent, not duplicated (Gate 3)
- [ ] Commits atomic and conventional, referencing the Issue
- [ ] PR created, CI green, review approved (Gate 4)
- [ ] Merged, deployed, smoke test passed, Issue closed (Gate 5)
- [ ] `traceability.md` complete from Issue through Release
