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
| Issue | #819 | open |
| Use Case | CU-16 (Archivar Gestión, #169); RF-22 (#22); RF-37 (#37) | exists |
| Specification | `openspec/changes/verify-debt-on-gestion-archive/` | in-progress |
| Branch | `feat/819_verify-debt-on-gestion-archive` | created |
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
| Gestión with a single trámite and presupuesto | `GestionArchiveDebtServiceTest#shouldReturnSinglePresupuestoBalanceForSingleTramite` | written, failing (compile) |
| Gestión with multiple trámites and presupuestos | `GestionArchiveDebtServiceTest#shouldSumBalancesAcrossMultipleTramitesAndPresupuestos` | written, failing (compile) |
| Gestión with no pending balance | `GestionArchiveDebtServiceTest#shouldReturnZeroWhenAllPresupuestosAreFullyPaid` | written, failing (compile) |
| Archiving a gestión with pending debt | `GestionArchiveIntegrationTest#shouldReportPendingDebtWhenArchivingGestionWithBalance` | written, failing (compile) |
| Confirming archiving despite pending debt | `GestionArchiveIntegrationTest#shouldArchiveGestionEvenWhenPendingDebtExists` | written, failing (compile) |
| Archiving a gestión with no pending debt | `GestionArchiveIntegrationTest#shouldArchiveGestionWithoutDebtWarning` | written, failing (compile) |
| Archiving record reflects pending debt | `GestionArchiveIntegrationTest#shouldPersistDeudaPendienteTrueWhenBalanceIsPositive` | written, failing (compile) |
| Archiving record reflects no pending debt | `GestionArchiveIntegrationTest#shouldPersistDeudaPendienteFalseWhenBalanceIsZero` | written, failing (compile) |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | no | |
| `docs/200-architecture/204-diagrams/Diagrama de Estados/transicion-de-estados.puml` | no | |
| `CHANGELOG.md` | no | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
