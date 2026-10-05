> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1210 exists, labeled, linked to CU76 (umbrella #1190; amendment #1210; guard wiring #1209)
- [x] 1.2 Use Case documentation exists (CU76); add #1210 to its ID table at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (relocation and packaging; Constitution wording is #1210)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b docs/1210_constitution_playwright_path`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh docs-1210-constitution-playwright-path`
- [ ] 2.5 Merge `main` once #1212 (#1192) is merged

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: stale path in the Constitution, E2E command, agent rule files
- [ ] 3.2 Remove the `CONSTITUTION.md` exemption from `scripts/test_testing_standalone.py`; observe `E2ELegacyReferenceTest` fail
- [ ] 3.3 Confirm every `#### Scenario:` maps to a test or a verification command

## 4. Implementación

- [ ] 4.1 Edit `CONSTITUTION.md` §4, §5 step 15 and §7 to `testing/e2e`
- [ ] 4.2 Bump the "Last reviewed" date
- [ ] 4.3 Guards green

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

- [ ] 7.1 n/a — no UI change; E2E suite untouched
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
- [ ] 9.2 Only the final commit may carry `Closes #1210`; others `Refs #1210`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin docs/1210_constitution_playwright_path` without `PREFLIGHT_SKIP`
- [ ] 10.3 Open PR `[#1210] docs(constitution): point the Playwright suite at testing/e2e`
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
- [ ] 12.3 Close Issue #1210 referencing the PR; update #1190; confirm #1210 is next
- [ ] 12.4 Archive the change: `openspec archive docs-1210-constitution-playwright-path`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guards observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright suite green from its new home (7.1)
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1210` on the last
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
