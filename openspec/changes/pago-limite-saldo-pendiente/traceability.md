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
| Issue | #848 | open |
| Use Case | CU15 – Procesar pago (#168) / RF-18 "Abonar trámite" (#20) | exists |
| Specification | `openspec/changes/pago-limite-saldo-pendiente/` | in progress |
| Branch | `fix/848_reject-payments-exceeding-saldo` | not yet created |
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
| Payment within the saldo pendiente is accepted | TBD — `PagoServiceTest` (unit) | pending |
| Payment exactly matching the saldo pendiente is accepted | TBD — `PagoServiceTest` (unit) | pending |
| Payment exceeding the saldo pendiente is rejected | TBD — `PagoServiceTest` (unit) + `PagoControllerTest` (integration, HTTP 409) | pending |
| Saldo pendiente calculation already accounts for prior payments | TBD — `PagoServiceTest` (unit) | pending |
| Operator sees a specific message when a payment is rejected for exceeding saldo | TBD — Playwright E2E, `frontend/tests/` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar pago.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | Issue #848 open; proposal/specs drafted, design/tasks not yet written |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
