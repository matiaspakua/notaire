# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1548 | open → in progress |
| Use Case | CU84 – Login al sistema (expired-session alternate flow) | exists |
| Specification | `docs/openspec/changes/fix-1548-session-expiry-redirect-race/` | Gate 1 draft |
| Branch | `fix/1548_session_expiry_redirect_race` | created from updated `main` |
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
| Stale markers at the edge | `edge-proxy.test.ts` | passing |
| Guard during expiry | `dashboard-layout-session-expiry.test.tsx` | passing |
| Under load | TS-0093 | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1548-session-expiry-redirect-race` |
| 2 | Failing tests written, test cases designed | yes | 1 Vitest proxy failure and 1 layout failure before the change; TS-0093 3/60 and 2/30 failures under load |
| 3 | Suite green, coverage held, docs updated | yes | frontend verify.sh / quick.sh (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
