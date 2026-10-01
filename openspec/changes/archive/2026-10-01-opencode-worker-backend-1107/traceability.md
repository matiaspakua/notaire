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
| Issue | #1107 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/opencode-worker-backend-1107/` | written |
| Branch | `feat/1107_opencode_worker_backend` | created |
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `b7592d8` (plan) … final archive commit; tests first: `14cfd3c` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The worker agent is selectable (4 scenarios) | `local-ai/sdlc/tests/test_worker.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/README.md` | yes | `8271add` |
| `local-ai/sdlc/AI-SDLC.md` | yes | `8271add` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1107, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `14cfd3c` failing (no worker module) before `59f1de6` |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green; OpenCode + gpt-oss 8/8 on the TDD smoke task; worker.py end to end with both agents; foreman runs of #1062 triage and spec phases with AGENT=opencode; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI setup and
  harness. The repair and patcher have Python `unittest` suites; the `with_retries`
  check is shell, verified by `bash -n` and a replay of the #1049 spec phase.
