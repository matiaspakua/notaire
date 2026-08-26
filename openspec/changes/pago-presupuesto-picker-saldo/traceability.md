# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #796 | open |
| Use Case | CU15 – Procesar pago (#168), pasos 2-5 y 11 | exists |
| Specification | `openspec/changes/pago-presupuesto-picker-saldo/` | in progress |
| Branch | `feat/796_pago-presupuesto-picker-saldo` | not yet created |
| Tasks | `tasks.md` | pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Operator selects a presupuesto from the picker | TBD — Playwright E2E, `frontend/tests/` | pending |
| Operator picks a presupuesto and it becomes the payment target | TBD — Playwright E2E, `frontend/tests/` | pending |
| No presupuestos available | TBD — component/unit test, `pagos/page.tsx` | pending |
| Saldo pendiente is shown after selecting a presupuesto | TBD — Playwright E2E, `frontend/tests/` | pending |
| Saldo pendiente updates when the selection changes | TBD — Playwright E2E, `frontend/tests/` | pending |
| Saldo pendiente fails to load | TBD — component/unit test, `pagos/page.tsx` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | Issue #796 open; proposal/specs drafted, design/tasks not yet written |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
