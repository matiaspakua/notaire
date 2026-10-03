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
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The suite and its tooling live under testing/e2e | `scripts/test_testing_standalone.py` | pending |
| The old locations are gone | same | pending |
| No spec was lost | same (51) | pending |
| Frontend is free of Playwright | same | pending |
| e2e does not reference paths outside itself | same | pending |
| The reliability guard runs from the discovered test directory | `python3 -m unittest discover -s scripts/tests` | pending |
| The reliability guard fails on a violation | mutation check on `scripts/test_e2e_reliability.py` | pending |
| Type-check and lint pass | `tsc --noEmit`, `eslint .` in `testing/e2e` | pending |
| CI and preflight run the moved suite | `scripts/test_testing_standalone.py` | pending |
| The same suite passes after the move | `run_pipeline.sh`, baseline 530 / 0 / 14 | pending |
| No live reference to frontend/tests/e2e | `scripts/test_testing_standalone.py` | pending |

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
