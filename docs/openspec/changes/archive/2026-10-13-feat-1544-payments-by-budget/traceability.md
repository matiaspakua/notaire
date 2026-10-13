# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1544 | open → in progress |
| Use Case | CU47 – Consultar pago; CU15 – Registrar pago | exists |
| Specification | `docs/openspec/changes/feat-1544-payments-by-budget/` | Gate 1 draft |
| Branch | `feat/1544_payments_by_budget` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Consult | TS-0014 CU47-BUDGET-01, Vitest | passing |
| URL | TS-0014, Vitest | passing |
| Empty | Vitest | passing |
| Mobile | TS-0014 CU47-BUDGET-02 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `contracts/api-reachability-allowlist.yaml` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1544-payments-by-budget` |
| 2 | Failing tests written, test cases designed | yes | 6 of 7 Vitest cases failed before the change (hooks missing) |
| 3 | Suite green, coverage held, docs updated | yes | frontend verify.sh / quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
