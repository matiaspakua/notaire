> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — §5 Official SDLC
> Workflow, §6 Quality Gates. Groups 1-12 are **mandatory**.

## 1. Gate 1 — Prerequisites

- [x] 1.1 GitHub Issue #1185 exists, labeled, and linked to CU76
- [x] 1.2 Use Case documentation exists (CU76); add #1185 to its GitHub ID table at implement
- [x] 1.3 Acceptance Criteria defined as scenarios in the delta spec
- [x] 1.4 Impact Analysis and affected modules confirmed in `proposal.md`
- [x] 1.5 ADR — n/a (housekeeping and tooling)
- [x] 1.6 Move the Issue to IN PROGRESS (`in-progress` label added)

## 2. Crear branch

- [x] 2.1 `git checkout main && git pull origin main`
- [x] 2.2 `git checkout -b ci/1185_clear_pipeline_failures`
- [x] 2.3 Branch name recorded in `traceability.md`
- [x] 2.4 Run `bash scripts/validate-sdlc-plan.sh ci-1185-clear-pipeline-failures`

## 3. Gate 2 — Escribir tests (TDD, failing first)

- [x] 3.1 Enumerate test cases: closed-issue changes, seed on BSD sed, unique headings, no entry lost, pipeline
- [x] 3.2 Record the red baseline: `validate-sdlc-plan.sh` (37 problems) and `test_seed_fills_known_header_values`
- [x] 3.3 Write failing `scripts/test_changelog_structure.py` and observe it fail
- [x] 3.4 Confirm every `#### Scenario:` maps to a test or a verification command

## 4. Implementación

- [x] 4.1 Make `scripts/seed-openspec-change.sh` portable (no `sed -i`)
- [x] 4.2 Regroup `CHANGELOG.md` `[Unreleased]` with entries preserved
- [x] 4.3 Finalise traceability of the #1179 and #1186 changes (PRs, merge commits, Gate 5)
- [x] 4.4 Archive the 45 changes with closed-COMPLETED issues; record any `--skip-specs` in design.md
- [x] 4.5 `validate-sdlc-plan.sh` exits 0; `openspec validate --strict` clean on active changes

## 5. Actualizar tests existentes

- [x] 5.1 `scripts/tests/test_validate_sdlc_plan.py` fully green
- [x] 5.2 All `scripts/test_*.py` guards still pass
- [x] 5.3 No gate loosened or skipped

## 6. Ejecutar regresión

- [x] 6.1 `mvn test -pl backend-api` — n/a (no Java touched)
- [x] 6.2 `mvn jacoco:check -pl backend-api` — n/a
- [x] 6.3 `mvn verify -pl backend-api` — n/a
- [x] 6.4 Bruno/HTTP suites via `bash scripts/run_pipeline.sh`
- [x] 6.5 No `@Disabled` tests

## 7. Ejecutar Playwright

- [x] 7.1 n/a product UI — no spec edits
- [x] 7.2 Required Playwright CI job must still pass on the PR
- [x] 7.3 n/a responsive UI checks
- [x] 7.4 Record "n/a — no UI surface"

## 8. Gate 3 — Actualizar documentación permanente

- [x] 8.1 `CI-PREFLIGHT.md` portability note
- [x] 8.2 CU76 ID table and `CHANGELOG.md` entry
- [x] 8.3 Confirm archived deltas are folded into `openspec/specs/`
- [x] 8.4 Confirm no information was duplicated
- [x] 8.5 `bash scripts/preflight.sh` with no bypass

## 9. Commits atómicos

- [x] 9.1 Separate commits: seed fix, CHANGELOG guard + regroup, #1179/#1186 traceability, archive sweep
- [x] 9.2 Only the final commit may carry `Closes #1185`; others `Refs #1185`
- [x] 9.3 No secrets, no commented-out code, no unrelated changes
- [x] 9.4 Record commit SHAs in `traceability.md`

## 10. Pull Request y validación CI

- [x] 10.1 `bash scripts/run_pipeline.sh` exits 0
- [x] 10.2 `git push -u origin ci/1185_clear_pipeline_failures` without `PREFLIGHT_SKIP`
- [x] 10.3 Open PR `[#1185] ci(sdlc): clear pre-existing failures that keep the pipeline red`
- [x] 10.4 Wait for all required workflows to pass
- [x] 10.5 Gate 4 — CI green, review approved, no conflicts, docs complete
- [x] 10.6 Record PR number in `traceability.md`

## 11. Deploy

- [x] 11.1 Owner merges via the PR — never push to `main`
- [x] 11.2 Confirm `cd.yml` ran green on `main`
- [x] 11.3 Record the merge commit in `traceability.md`

## 12. Gate 5 — Smoke test y cierre

- [x] 12.1 Smoke: clean `main` passes `validate-sdlc-plan.sh`; a normal `git push` is not blocked
- [x] 12.2 Verify rollback path (revert PR) still valid
- [x] 12.3 Close Issue #1185 referencing the PR
- [x] 12.4 Archive this change: `openspec archive ci-1185-clear-pipeline-failures`

## Definition of Done

- [x] Issue linked to a Use Case, with Acceptance Criteria
- [x] Specification written and reviewed (Gate 1)
- [x] Test cases designed; failing checks observed (Gate 2)
- [x] Implementation passes guards and required CI (Gate 3–4)
- [x] Coverage gate unaffected
- [x] Playwright n/a (no UI) but required CI jobs green
- [x] Permanent documentation updated and consistent
- [x] Commits atomic, Conventional Commits
- [x] Pull Request created, CI green, review approved (Gate 4)
- [x] Merged via PR; smoke evidence recorded; Issue closed (Gate 5)

## Closure note (2026-10-04)

Shipped in PR #1193 (merged) and verified on `main`. The checklist was ticked at archive time from that evidence.
