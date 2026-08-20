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
| Issue | #819 | closed |
| Use Case | CU-16 (Archivar Gestión, #169); RF-22 (#22); RF-37 (#37) | exists |
| Specification | `openspec/changes/verify-debt-on-gestion-archive/` | in-progress |
| Branch | `feat/819_verify-debt-on-gestion-archive` | merged |
| Tasks | `tasks.md` | in-progress (Gate 5 items open) |
| Commits | a8fad05, 9d33de2, be7c26c, deab0e8, d4f74a8 | done |
| Pull Request | [#826](https://github.com/matiaspakua/notaire/pull/826) | merged 2026-08-20T11:30:39Z |
| CI run | CI/Frontend CI/PR Validation green on PR head `d4f74a8`; `Playwright E2E — Full Suite` failed, but the same suite fails identically on `main` pre-merge (unrelated `01-first-case-tutorial.spec.ts` flake) | green except pre-existing flake |
| Merge commit | `dc5e2a3` | merged to `main` |
| Release / tag | | pending — CD (`cd.yml`) has not run for `dc5e2a3` yet, no image published to GHCR for this commit |
| Smoke test | | pending — not yet performed |

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
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #819, `openspec/changes/verify-debt-on-gestion-archive/` proposal+specs+design |
| 2 | Failing tests written, test cases designed | yes | `GestionArchiveDebtServiceTest`, `GestionArchiveIntegrationTest`, `crud-gestiones.spec.ts` written before implementation (commit a8fad05) |
| 3 | Suite green, coverage held, docs updated | yes | CI green on PR head `d4f74a8`; docs updated in commit `d4f74a8` |
| 4 | CI green, review approved, no conflicts | partial | CI green except pre-existing `Playwright E2E — Full Suite` flake (also fails on `main`); no recorded review approval (`reviewDecision` empty on PR #826) |
| 5 | Deployed, smoke test passed, Issue closed | no | Issue #819 closed, but CD has not run for merge commit `dc5e2a3` and no smoke test has been performed |

## Exceptions

None.
