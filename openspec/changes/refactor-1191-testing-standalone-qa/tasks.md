> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1191 exists, labeled, and linked to CU76 (umbrella #1190, phase 2 #1192)
- [x] 1.2 Use Case documentation exists (CU76, CU75); add #1191 to both ID tables at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta specs
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (same pattern as #1179 applied to QA; no architectural decision)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b refactor/1191_testing_standalone_qa`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh refactor-1191-testing-standalone-qa`
- [x] 2.5 Merge `main` after #1193 landed so the pipeline gates are green

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [ ] 3.1 Enumerate test cases: layout, deletions, runner, database checks (incl. placeholders, R14), pins/no host port, self-containment, docs, gates
- [ ] 3.2 Write failing `scripts/test_testing_standalone.py` and observe it fail
- [ ] 3.3 Check that `test_image_pins_and_dependabot.py` covers the new compose file's pins; observe it fail first if it does not
- [ ] 3.4 Write the database SQL checks before the compose harness exists; observe the first run fail
- [ ] 3.5 Confirm every `#### Scenario:` maps to a test or a verification command

## 4. Implementación

- [ ] 4.1 `git mv` the cURL suite and stack smoke into `testing/integration/` (separate commit, no content change)
- [ ] 4.2 Leave `infra/` untouched (k6 stays in `infra/performance`, Owner decision)
- [ ] 4.3 Delete the five unreferenced scripts and `testing/reports/*`; ignore `testing/reports/`
- [ ] 4.4 Write `scripts/run.sh`, make `scripts/test.sh` a wrapper, fold the stack smoke into `integration`
- [ ] 4.5 Build `testing/database/` (compose, Flyway settings, SQL checks, negative check)
- [ ] 4.6 Add `testing/.env.example`; resolve paths relative to `testing/`; mark the seams
- [ ] 4.7 Repoint workflows, scripts, agent rules and docs to the new paths
- [ ] 4.8 Add `database-vv.yml` and the `preflight.sh --full` and `--list` entries together
- [ ] 4.9 All guards green; `R14__` resolved as a deliberate manual rollback, asserted by the suite

## 5. Actualizar tests existentes

- [ ] 5.1 Existing guards pass with assertions unchanged except the deliberate infra layout amendment
- [ ] 5.2 Fix docs and agent rules that reference removed scripts
- [ ] 5.3 No dead references to the removed scripts remain

## 6. Ejecutar regresión

- [ ] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [ ] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [ ] 6.3 `mvn verify -pl backend-api` — n/a
- [ ] 6.4 Bruno and the cURL suite via `bash scripts/run_pipeline.sh`
- [ ] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [ ] 7.1 n/a product UI — Playwright is untouched in this phase
- [ ] 7.2 Required Playwright CI job must still pass on the PR
- [ ] 7.3 n/a responsive UI checks
- [ ] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [ ] 8.1 Write `testing/README.md` and the four guides under `testing/docs/`
- [ ] 8.2 Update 303-testing README and TEST-PLAN, CI-PREFLIGHT, TEST-COVERAGE-STRATEGY, CLAUDE.md, AGENTS.md
- [ ] 8.3 CU76 and CU75 ID tables; `CHANGELOG.md`
- [ ] 8.4 Archive superseded docs under `docs/000-archive/` if any
- [ ] 8.5 Confirm no information is duplicated between `docs/` and `testing/`
- [ ] 8.6 `bash scripts/preflight.sh` without bypass

## 9. Commits atómicos

- [ ] 9.1 Separate commits: red guards, moves, deletions, runner, database suite, repointing, CI and preflight, docs
- [ ] 9.2 Only the final commit may carry `Closes #1191`; others `Refs #1191`
- [ ] 9.3 No secrets, no commented-out code, no unrelated changes
- [ ] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [ ] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [ ] 10.2 `git push -u origin refactor/1191_testing_standalone_qa` without `PREFLIGHT_SKIP`
- [ ] 10.3 Open PR `[#1191] refactor(testing): restructure testing/, add database V&V suite`
- [ ] 10.4 Wait for all required workflows, including the new `database-vv.yml`
- [ ] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [ ] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [ ] 11.1 Owner merges via the PR — never push to `main`
- [ ] 11.2 Confirm `cd.yml` ran green on `main`
- [ ] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [ ] 12.1 Smoke: `run.sh database` green on merged `main`; `test.sh` green against a running stack; `database-vv.yml` green
- [ ] 12.2 Verify rollback path (revert PR) still valid
- [ ] 12.3 Close Issue #1191 referencing the PR; update #1190
- [ ] 12.4 Archive the change: `openspec archive refactor-1191-testing-standalone-qa`

## Definition of Done

- [ ] Issue linked to a Use Case, with Acceptance Criteria
- [ ] Specification written and reviewed (Gate 1)
- [ ] Test cases designed; failing guards and database checks observed (Gate 2)
- [ ] Implementation passes guards and required CI (Gate 3–4)
- [ ] Coverage gate unaffected
- [ ] Playwright n/a (no UI) but required CI jobs green
- [ ] Permanent documentation updated and consistent
- [ ] Commits atomic, Conventional Commits, `Closes #1191` on the last
- [ ] Pull Request created, CI green, review approved (Gate 4)
- [ ] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)
