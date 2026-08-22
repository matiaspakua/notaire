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
| Issue | #820 | open, in-progress |
| Use Case | CU-47 (Consultar Pago, #200); CU-02 (Iniciar Gestión, #155) | exists |
| Specification | `openspec/changes/payment-financial-tracking/` | complete |
| Branch | `feat/820_expose-pago-presupuesto-gestion-summary` | created |
| Tasks | `tasks.md` | in progress — see checklist |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Retrieving a single payment includes its presupuesto | `PagoControllerTest#shouldIncludePresupuestoWhenRetrievingPaymentById` | passing |
| Listing payments by presupuesto includes the presupuesto on each entry | `PagoControllerTest#shouldIncludePresupuestoOnEachListedPayment` | passing |
| Creating a payment returns the associated presupuesto | `PagoControllerTest#shouldReturnPresupuestoWhenCreatingPayment` | passing |
| Presupuesto with no payments | `PresupuestoResumenServiceTest#shouldReturnFullBalanceWhenNoPayments` | passing |
| Presupuesto with one payment | `PresupuestoResumenServiceTest#shouldReturnReducedBalanceWithOnePayment` | passing |
| Presupuesto with multiple payments | `PresupuestoResumenServiceTest#shouldReturnNetBalanceWithMultiplePayments` | passing |
| Requesting the summary of a non-existent presupuesto | `PresupuestoResumenControllerTest#shouldReturnNotFoundForUnknownPresupuesto` | passing |
| Gestión with a single trámite and presupuesto | `GestionResumenFinancieroServiceTest#shouldSummarizeSingleTramiteGestion` | passing |
| Gestión with multiple trámites and presupuestos | `GestionResumenFinancieroServiceTest#shouldAggregateMultipleTramites` | passing |
| Gestión with no payments registered | `GestionResumenFinancieroServiceTest#shouldReturnZeroCollectedWhenNoPayments` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` | yes | pending (uncommitted) |
| `CHANGELOG.md` | yes | pending (uncommitted) |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #820 open, in-progress; proposal/design/specs complete |
| 2 | Failing tests written, test cases designed | yes | Unit + integration tests written first, observed failing, now passing |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api -am`: 1513 tests, BUILD SUCCESS; `tsc --noEmit` and `eslint --max-warnings=0` clean; docs updated |
| 4 | CI green, review approved, no conflicts | pending | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None.
