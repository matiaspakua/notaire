# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1340 | open → in progress |
| Use Case | CU02 – Iniciar gestión; CU15 – Procesar pago | exists |
| Specification | `docs/openspec/changes/fix-1340-budget-picker/` | Gate 1 draft |
| Branch | `fix/1340_budget_picker` | created from updated `main` |
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
| Oldest budget by number | `testing/e2e/tests/TS-0114-budget-picker.spec.ts` | passing |
| Budget by client name | `testing/e2e/tests/TS-0114-budget-picker.spec.ts` | passing |
| Combobox semantics | `frontend/src/tests/unit/budget-picker.test.tsx` | passing |
| No size=1000 for budgets | `frontend/src/tests/unit/budget-picker.test.tsx` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | yes | branch commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh fix-1340-budget-picker` |
| 2 | Failing tests written, test cases designed | yes | `budget-picker.test.tsx` (module missing) and `TS-0114` (2 failed: no searchable budget combobox) observed failing before the change |
| 3 | Suite green, coverage held, docs updated | yes | `bash frontend/verify.sh` green; Playwright chromium suite against the branch build |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

`workspace/sdlc/run_pipeline.sh` needs Docker, unavailable on the agent box; Playwright ran against a production build (`next build` standalone on :9090) and the backend from `main` on :8080.
