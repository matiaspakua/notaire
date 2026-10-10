# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU18 – Consultar persona; CU54 – Listar clientes; CU61 – Buscar persona o cliente | exists |
| Specification | `docs/openspec/changes/fix-1340-people-pagination/` | Gate 1 draft |
| Branch | `fix/1340_people_pagination` | created from updated `main` |
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
| Page and total | `testing/e2e/tests/TS-0110-people-pagination.spec.ts` | passing |
| Oldest person reachable | `testing/e2e/tests/TS-0110-people-pagination.spec.ts` | passing |
| Duplicate link outside the page | `testing/e2e/tests/TS-0110-people-pagination.spec.ts`, `frontend/src/tests/unit/personas-pagination.test.tsx` | passing |
| Hook and URL | `frontend/src/tests/unit/personas-pagination.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-people-pagination` |
| 2 | Failing tests written, test cases designed | yes | `personas-pagination.test.tsx` (4 failed, then 1 and 1 for the toast and out-of-range follow-ups) and `TS-0110` (2 failed: 1000 rows; toast link) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
