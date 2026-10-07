# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1085 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/aisdlc-project-adapter-1085/` | written |
| Branch | `chore/1085_aisdlc_project_adapter` | created |
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `ff6e2b1` (plan) … final archive commit; tests first: `824377e`, `0996d37` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Project values come from the adapter (3 scenarios) | `local-ai/sdlc/tests/test_adapter.py` | green |
| Surfaces are derived from the adapter (4 scenarios) | `local-ai/sdlc/tests/test_adapter.py` | green |
| TEST_CMD uses the surface's single-test command (2 scenarios) | `local-ai/sdlc/tests/test_adapter.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | yes | `0000d03` |
| `local-ai/AUDIT.md` | yes | `5c44923` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1085, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `824377e`, `0996d37` committed and failing before `df33e4b`, `3ff144f` |
| 3 | Suite green, coverage held, docs updated | yes | 33 harness self-tests green; `preflight.sh` passed (17/17 run); no backend code changed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness
  and its config. The tests are Python `unittest` suites.
