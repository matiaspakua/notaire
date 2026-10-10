# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU01 – Preparar presupuesto; CU60 – Buscar presupuestos por estado; CU06 – Preparar escritura; CU07 – Consultar escritura | exists |
| Specification | `docs/openspec/changes/fix-1340-budgets-deeds-pagination/` | Gate 1 draft |
| Branch | `fix/1340_budgets_deeds_pagination` | stacked on #1396 |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | pushed |
| Pull Request | #1397 | open |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Page and total | `testing/e2e/tests/TS-0113-budgets-deeds-pagination.spec.ts`, `frontend/src/tests/unit/budgets-deeds-pagination.test.tsx` | passing |
| Oldest row reachable | `testing/e2e/tests/TS-0113-budgets-deeds-pagination.spec.ts` | passing |
| Dashboard total | `testing/e2e/tests/TS-0113-budgets-deeds-pagination.spec.ts` | passing |
| Filters reach the backend | `testing/e2e/tests/TS-0113-budgets-deeds-pagination.spec.ts`, `frontend/src/tests/unit/budgets-deeds-pagination.test.tsx` | passing |
| Budget number on any page | `frontend/src/tests/unit/budgets-deeds-pagination.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-budgets-deeds-pagination` |
| 2 | Failing tests written, test cases designed | yes | `budgets-deeds-pagination.test.tsx` (7 failed) and `TS-0113` observed failing on the #1396 build |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
