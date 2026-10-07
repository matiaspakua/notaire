# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1250 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1250-endpoint-reachability-guard/` | Gate 1 draft |
| Branch | `fix/1250_endpoint_reachability_guard` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | local, not pushed |
| Pull Request | — | pending (Owner approval) |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Unreferenced endpoint fails | `contracts/tests/test_api_reachability.py` | passing |
| Stale allowlist entry fails | `contracts/tests/test_api_reachability.py` | passing |
| Scanner binds literals to endpoints | `contracts/tests/test_api_reachability.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `contracts/MODULE.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh fix-1250-endpoint-reachability-guard` |
| 2 | Failing tests written, test cases designed | yes | guard written first and observed failing (allowlist missing) |
| 3 | Suite green, coverage held, docs updated | partial | contracts, workspace, changelog and links guards green; run_pipeline.sh not run (no Docker) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`scripts/run_pipeline.sh` needs Docker, unavailable on the agent box.
