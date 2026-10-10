# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1367 | open → in progress |
| Use Case | RNF-10 – Seguimiento del trabajo sobre ventanas; RNF-06 – Usabilidad; CU78 – Security and Compliance (admin-only navigation) | exists |
| Specification | `docs/openspec/changes/feat-1367-grouped-sidebar/` | Gate 1 draft |
| Branch | `feat/1367_grouped_sidebar` | created from updated `main` |
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
| Admin sees six sections | `TS-0121`, `app-sidebar.test.tsx` | passing |
| Collapse persists | `TS-0121`, `app-sidebar.test.tsx` | passing |
| Shared config | `dashboard-nav.test.ts`, `TS-0121` | passing |
| Non-admin | `TS-0121`, `app-sidebar.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `frontend/messages/{es,en}.json` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1367-grouped-sidebar` |
| 2 | Failing tests written, test cases designed | yes | 11 Vitest failures and TS-0121 4/4 failing on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; full chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
