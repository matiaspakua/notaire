# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1358 | open → in progress |
| Use Case | RNF-03 – Tiempo de respuesta; RF-23 | exists |
| Specification | `docs/openspec/changes/fix-1358-dashboard-stats-skeleton/` | Gate 1 draft |
| Branch | `fix/1358_dashboard_stats_skeleton` | created from updated `main` |
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
| Loading | `frontend/src/tests/unit/dashboard-stat-value.test.tsx` | passing |
| Loaded | `testing/e2e/tests/TS-0118-dashboard-stats.spec.ts` | passing |
| No list downloads | `testing/e2e/tests/TS-0118-dashboard-stats.spec.ts` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1358-dashboard-stats-skeleton` |
| 2 | Failing tests written, test cases designed | yes | dashboard-stat-value.test.tsx and TS-0118 observed failing before the change (test commit) |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
