# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```unknown
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1192 (phase 2 of #1190) | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists (add #1192 to the ID table) |
| Related | #1191 / PR #1196 (phase 1), #1210 (Constitution amendment), #1209 (guard wiring; lands first), #1066 and #1146 (the reliability rules ported) | referenced |
| Specification | `openspec/changes/refactor-1192-playwright-to-testing/` | Gate 1 draft |
| Branch | `refactor/1192_playwright_to_testing` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | `74cf3dc` red guards, `a6b964b` pure rename, `43b0e9e` constants + packaging, `e254bee` frontend removal, `b6b5c9d` gates, `5aa6670` docs | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The suite and its tooling live under testing/e2e | `scripts/test_testing_standalone.py` | passed |
| The old locations are gone | same | passed |
| No spec was lost | same (52) | passed |
| Frontend is free of Playwright | same | passed |
| e2e does not reference paths outside itself | same | passed |
| The reliability guard runs from the discovered test directory | `python3 -m unittest discover -s scripts/tests` | passed |
| The reliability guard fails on a violation | mutation check on `scripts/test_e2e_reliability.py` | passed |
| Type-check and lint pass | `tsc --noEmit`, `eslint .` in `testing/e2e` | passed |
| CI and preflight run the moved suite | `scripts/test_testing_standalone.py` | passed |
| The same suite passes after the move | full run from `testing/e2e`: 535 passed, 0 failed, 14 skipped (baseline 530 plus 5 tests of `workflow-tracker.spec.ts`, added by #1208 after the baseline); one flaky test passed on retry in the second run; the first run had one failure (TS-0093) that passed alone and on the second run | done |
| No live reference to frontend/tests/e2e | `scripts/test_testing_standalone.py` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `testing/README.md`, `testing/docs/*`, `testing/.env.example` | pending | — |
| 303-testing docs, ADR-005, workflow tracker, CI-PREFLIGHT | pending | — |
| README, AGENTS.md, CLAUDE.md, rules, skills, schema templates | pending | — |
| CU76 | pending | — |
| `CHANGELOG.md` | pending | — |
| `CONSTITUTION.md` | not in this change (#1210) | n/a |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh refactor-1192-playwright-to-testing` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.

## Verification log (2026-10-04, cloud session)

- Red first: before the move the new guards failed (8 failures, 1 error in `test_testing_standalone.py`; 12 errors/failures in the reliability guard); after: 31 and 12 tests green; `python3 -m unittest discover -s scripts/tests` 148 tests OK.
- Mutation check: `waitForTimeout(` appended to TS-0040 and retries set to 2 made 2 reliability tests fail; reverted.
- `testing/e2e`: `npm ci`, `tsc --noEmit`, `eslint . --max-warnings=0` exit 0.
- Frontend: `tsc --noEmit`, `eslint src --max-warnings=0`, `vitest run --coverage` (15.37% statements, 10.54% branches, 12.2% functions, 15.79% lines; floors unchanged), `next build` OK.
- Not run in this environment: `scripts/run_pipeline.sh` (the Docker image build cannot reach the network here; the stack was run natively: local PostgreSQL 16, jar, `next start`). CI runs the full gates on the PR.
- Spec count is 52, not 51: #1208 added `workflow-tracker.spec.ts` after the spec was written.
