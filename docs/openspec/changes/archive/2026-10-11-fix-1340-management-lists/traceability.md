# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU10 – Registrar movimientos documentación de entidades externas; CU43 – Reingresar documentación; CU04 – Registrar documentación cliente | exists |
| Specification | `docs/openspec/changes/fix-1340-management-lists/` | Gate 1 draft |
| Branch | `fix/1340_management_lists` | created from updated `main` |
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
| Newest case on page one | `management-lists-pagination.test.tsx`, `TS-0018`, `TS-0033` | passing |
| Page past the end | `management-lists-pagination.test.tsx` | passing |
| Pick any management | `management-lists-pagination.test.tsx`, `TS-0099`, `TS-0090` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |
| `frontend/messages/{es,en}.json` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-management-lists` |
| 2 | Failing tests written, test cases designed | yes | Vitest red (module missing); TS-0018/0033/0099/0090 failing on the pre-change build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; full chromium suite (see PR) |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
