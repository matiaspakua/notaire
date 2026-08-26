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
| Issue | #820 | closed |
| Use Case | CU-47 (Consultar Pago, #200); CU-02 (Iniciar Gestión, #155) | exists |
| Specification | `openspec/changes/payment-financial-tracking/` | complete |
| Branch | `feat/820_expose-pago-presupuesto-gestion-summary` | merged |
| Tasks | `tasks.md` | complete — see checklist |
| Commits | `de89f30`, `1436d0a`, `049371e`, `d5b145e` | landed |
| Pull Request | [#845](https://github.com/matiaspakua/notaire/pull/845) | merged 2026-08-23 |
| CI run | PR #845 checks green (required for merge per branch protection) | passed |
| Merge commit | `a2a17f8824b53f0a3d1c11f28fc3facafdcfdf22` (main) | merged 2026-08-23T09:27:36Z |
| Release / tag | no git tag cut for this merge — this repo deploys continuously to `main` via `cd.yml` (no per-change tag) | n/a |
| Smoke test | not separately recorded at merge time; retroactively confirmed 2026-08-26 — `GET /presupuestos/{id}/resumen` and `GET /gestiones/{id}/resumen-financiero` exist on `main` and are exercised by `PresupuestoResumenControllerTest`/`GestionResumenFinancieroControllerTest` | passed (retroactive) |

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
| `docs/100-business/102-use-cases/CU47 – Consultar Pago.md` | yes | `d5b145e` |
| `CHANGELOG.md` | yes | `d5b145e` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #820 open, in-progress; proposal/design/specs complete |
| 2 | Failing tests written, test cases designed | yes | Unit + integration tests written first, observed failing, now passing |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify -pl backend-api -am`: 1513 tests, BUILD SUCCESS; `tsc --noEmit` and `eslint --max-warnings=0` clean; docs updated |
| 4 | CI green, review approved, no conflicts | yes | PR #845 merged 2026-08-23 into `main` (merge commit `a2a17f8`) |
| 5 | Deployed, smoke test passed, Issue closed | yes | Merged to `main` (continuously deployed via `cd.yml`); Issue #820 closed at merge; endpoints present and tested on `main` (see Smoke test row above) |

## Exceptions

This ledger was reconstructed retroactively on 2026-08-26 (during archival) from
`gh pr view 845`, `gh issue view 820` and `git log`, because the change had
already been merged and the Issue closed without this file being updated at
the time. Commit SHAs, PR and merge-commit references above are verified
against those sources; the smoke test and release/tag rows are marked
accordingly as retroactive/inferred rather than recorded live.
