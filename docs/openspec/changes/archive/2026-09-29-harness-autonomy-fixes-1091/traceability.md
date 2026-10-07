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
| Issue | #1091 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/harness-autonomy-fixes-1091/` | written |
| Branch | `chore/1091_harness_autonomy_fixes` | created |
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `6a34902` (plan) … final archive commit; tests first: `684a90e` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The harness repairs tasks.md to base plus ticks (3 scenarios) | `local-ai/sdlc/tests/test_ledger.py` | green |
| The spec gate requires the ledger rows (2 scenarios) | `local-ai/sdlc/tests/test_ledger.py` | green |
| The adapter provides a Markdown lint fix command (1 scenario) | `local-ai/sdlc/tests/test_adapter.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | yes | `d7cc312` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1091, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `684a90e` committed and failing (no `restore_ticks`/`missing_rows`, no adapter key) before `128962c`, `47ab614` |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green (8 new); `restore-ticks` on the #1063 `bad-docs` `tasks.md` drops `5.4` and the `[-]` items and keeps the ticks; no backend code changed; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness.
  The tests are Python `unittest` suites.
- This PR also archives `raise-jacoco-branch-floor-1063` (`fc2f75b`, `Refs #1063`):
  #1063 is closed, so its open change failed plan validation on every PR
  (precedent `3dfb3f0`).
