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
| Issue | #821 | open |
| Use Case | CU-15 – Procesar Pago (#168); CU-47 – Consultar Pago (#200) | exists |
| Specification | `openspec/changes/pagos-parciales-cuotas/` | in progress |
| Branch | `feat/821_pagos-parciales-cuotas` | pending |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Pago que cubre el total del presupuesto | `PagoServiceTest#shouldAcceptPaymentCoveringFullTotal` | pending |
| Pago parcial que no cubre el total | `PagoServiceTest#shouldAcceptPartialPaymentBelowSaldo` | pending |
| Secuencia de pagos parciales que suman el total | `PagoServiceIntegrationTest#shouldAcceptSequenceOfPartialPaymentsReachingTotal` | pending |
| Presupuesto sin pagos registrados | `PagoServiceTest#shouldReturnSinPagosStatusWhenNoPaymentsRegistered` | pending |
| Presupuesto parcialmente abonado | `PagoServiceTest#shouldReturnParcialStatusWhenBalancePending` | pending |
| Presupuesto saldado | `PagoServiceTest#shouldReturnSaldadoStatusWhenBalanceZero` | pending |
| Consultar el estado de un presupuesto parcialmente abonado | `PagoControllerTest#shouldReturnEstadoParcialForPresupuesto` | pending |
| Consultar el estado de un presupuesto saldado | `PagoControllerTest#shouldReturnEstadoSaldadoForPresupuesto` | pending |
| Consultar el estado de un presupuesto inexistente | `PagoControllerTest#shouldReturnNotFoundWhenPresupuestoDoesNotExist` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU15 – Procesar Pago.md` | no | — |
| `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | in progress |
| 2 | Failing tests written, test cases designed | no | — |
| 3 | Suite green, coverage held, docs updated | no | — |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.
