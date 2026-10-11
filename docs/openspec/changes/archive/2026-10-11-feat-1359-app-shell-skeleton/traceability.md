# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1359 | open → in progress |
| Use Case | RNF-03 – Tiempo de respuesta; RNF-05 – Usabilidad | exists |
| Specification | `docs/openspec/changes/feat-1359-app-shell-skeleton/` | Gate 1 draft |
| Branch | `feat/1359_app_shell_skeleton` | created from updated `main` |
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
| First paint is the app shell | `app-shell-skeleton.test.tsx`, `TS-0125` | passing |
| Real shell replaces it without layout shift | `app-shell-skeleton.test.tsx`, `TS-0125` | passing |
| No pulsing under reduced motion | `app-shell-skeleton.test.tsx` | passing |
| Loading lists show skeleton rows | `app-shell-skeleton.test.tsx`, `data-table.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1359-app-shell-skeleton` |
| 2 | Failing tests written, test cases designed | yes | Vitest red (modules missing); TS-0125 failing on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; full chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and a backend from current `main` on :8080.
