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
| Issue | #1093 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/harness-triage-autonomy-1093/` | written |
| Branch | `chore/1093_harness_triage_autonomy` | created |
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `85c4d48` (plan) … final archive commit; tests first: `2bec8b1` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Triage may name the surface its tests live on (3 scenarios) | `local-ai/sdlc/tests/test_adapter.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | yes | `b168767` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1093, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `2bec8b1` committed and failing (no `fallback` argument) before `c16a20c` |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green (4 new); `gate_scope` flow simulated (pass drops the violation, failure prepends it); no backend code changed; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness.
  The tests are Python `unittest` suites. `gate_scope` is shell glue checked by
  `bash -n` and the #1064 rerun.
