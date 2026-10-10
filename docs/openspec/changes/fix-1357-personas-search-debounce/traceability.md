# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1357 | open → in progress |
| Use Case | CU61 – Buscar persona o cliente; RF-39; RNF-03 | exists |
| Specification | `docs/openspec/changes/fix-1357-personas-search-debounce/` | Gate 1 draft |
| Branch | `fix/1357_personas_search_debounce` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | #1437 | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Typing a surname | `testing/e2e/tests/TS-0015-personas-clientes-workflow.spec.ts` (#1357), `frontend/src/tests/unit/personas-search.test.tsx` | passing |
| Refining a search | `frontend/src/tests/unit/personas-search.test.tsx` | passing |
| Clearing the search | `frontend/src/tests/unit/personas-search.test.tsx` (idle without criteria) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1357-personas-search-debounce` |
| 2 | Failing tests written, test cases designed | yes | `personas-search.test.tsx` (5 failed) and TS-0015 `#1357` (17 requests) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
