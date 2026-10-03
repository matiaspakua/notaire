> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1192 exists, labeled, linked to CU76 (umbrella #1190; amendment #1210; guard wiring #1209)
- [x] 1.2 Use Case documentation exists (CU76); add #1192 to its ID table at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (relocation and packaging; Constitution wording is #1210)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b refactor/1192_playwright_to_testing`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1192-playwright-to-testing`
- [ ] 2.5 Merge `main` after #1209 landed so the layout guard is enforced in CI

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: layout, old locations gone, spec count, frontend free of Playwright, self-containment, reliability rules, tsc and lint, gates and names, no live reference
- [ ] 3.2 Record the baseline: spec count (51) and the last full run (530 passed, 0 failed, 14 skipped)
- [ ] 3.3 Add the layout, frontend-free, self-containment and legacy-reference checks to `scripts/test_testing_standalone.py`; observe them fail
- [ ] 3.4 Port the reliability assertions to `scripts/test_e2e_reliability.py` with a wrapper in `scripts/tests/`; observe it fail
- [ ] 3.5 Confirm every `#### Scenario:` maps to a test or a verification command

## 4. Implementación

- [ ] 4.1 `git mv frontend/tests/e2e testing/e2e/tests` and `frontend/playwright.config.ts` to `testing/e2e/` (own commit, no content edits)
- [ ] 4.2 Rewrite `testDir`, `globalSetup`, `globalTeardown`, the reporter path and the two fixture paths
- [ ] 4.3 Add `package.json`, lockfile, `tsconfig.json`, `eslint.config.mjs`, `.gitignore` to `testing/e2e`; `tsc` and `eslint` clean
- [ ] 4.4 Remove Playwright from the frontend (dependencies, scripts, Vitest exclude, ESLint ignores, ignore files); lockfile diff reviewed to Playwright packages only
- [ ] 4.5 Delete `e2e-test-reliability.test.ts` once its Python port is green and mutation-checked
- [ ] 4.6 Repoint `playwright-e2e.yml`, `preflight.sh`, `run_pipeline.sh`, `generate_e2e_coverage_report.py`
- [ ] 4.7 Repoint docs, agent rules, skills, OpenSpec schema templates and README
- [ ] 4.8 All guards green; `CONSTITUTION.md` exempt with a comment naming #1210

## 5. Actualizar tests existentes

- [ ] 5.1 Frontend typecheck, ESLint, Vitest (coverage floors re-measured) and build pass
- [ ] 5.2 Existing guards pass with assertions unchanged
- [ ] 5.3 No dead references to the old location remain

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [ ] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 Bruno and the cURL suite via `bash scripts/run_pipeline.sh`
- [ ] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 Full suite from `testing/e2e`: 530 passed, 0 failed, 14 skipped
- [ ] 7.2 Required Playwright CI job passes on the PR; check names unchanged
- [ ] 7.3 Mobile project runs (WebKit installed)
- [ ] 7.4 Mutation check on the reliability guard recorded

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 `testing/README.md` and `testing/docs/*`, `testing/.env.example`
- [ ] 8.2 303-testing docs, ADR-005, workflow tracker, CI-PREFLIGHT, README, AGENTS.md, CLAUDE.md, rules, skills
- [ ] 8.3 CU76 ID table and `CHANGELOG.md`
- [ ] 8.4 Archive superseded docs under `docs/000-archive/` if any
- [ ] 8.5 Confirm no information is duplicated between `docs/` and `testing/`
- [ ] 8.6 `bash scripts/preflight.sh` without bypass

## 9. Commits atómicos

- [ ] 9.1 Separate commits: red tests, move, config and packaging, frontend removal, reliability port, gates, docs
- [ ] 9.2 Only the final commit may carry `Closes #1192`; others `Refs #1192`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin refactor/1192_playwright_to_testing` without `PREFLIGHT_SKIP`
- [ ] 10.3 Open PR `[#1192] refactor(testing): move the Playwright E2E suite to testing/e2e`
- [ ] 10.4 Wait for all required workflows, including `Playwright E2E`
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `playwright-e2e.yml` green on the merge commit; `cd testing/e2e && npx playwright test --project=smoke` passes locally
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1192 referencing the PR; update #1190; confirm #1210 is next
- [ ] 12.4 Archive the change: `openspec archive refactor-1192-playwright-to-testing`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guards observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright suite green from its new home (7.1)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1192` on the last
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
