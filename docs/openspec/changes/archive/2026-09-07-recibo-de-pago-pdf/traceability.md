# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #23 | closed |
| Use Case | CU15 – Procesar pago / RF-21 | exists |
| Specification | `openspec/changes/recibo-de-pago-pdf/` | complete |
| Branch | `feat/23_recibo-de-pago-pdf` | merged |
| Tasks | `tasks.md` | complete (Gates 1–5) |
| Commits | `3ad8e85`, `04e322f`, `d77a454`, `96302f4` | done |
| Pull Request | [#961](https://github.com/matiaspakua/notaire/pull/961) | merged |
| CI run | green (PR #961) | done |
| Merge commit | `96302f4` (2026-09-06) | done |
| Release / tag | standard CD pipeline, no separate tag | done |
| Smoke test | 404/problem+json confirmed live; 200/PDF path covered by `ReciboPagoReportIntegrationTest` | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Generar recibo para un pago existente devuelve PDF con cliente, fecha, concepto(s) y total | `ReporteServiceReciboPagoTest#shouldGenerarReciboConDatosDelPago`, `ReciboPagoReportIntegrationTest#shouldReturnPdfForRecibo` | done |
| Generar recibo para un `idPago` inexistente devuelve 404 | `ReporteServiceReciboPagoTest#shouldThrowWhenPagoNoExiste` | done |
| Recibo de un pago parcial/en cuotas incluye el monto de esa cuota, no el total del presupuesto | `ReporteServiceReciboPagoTest#shouldGenerarReciboParaPagoParcial` | done |
| E2E: click "Emitir recibo" descarga el PDF | `TS-0014-pagos-workflow.spec.ts#CU15-RECIBO-01` | done |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | yes | this commit |
| `backend-api/api-test/COVERAGE.md` | yes | `3ad8e85` |
| `CHANGELOG.md` | yes | this commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #23 open, linked to CU15; proposal.md + spec drafted |
| 2 | Failing tests written, test cases designed | yes | `ReporteServiceReciboPagoTest`, `ReciboPagoReportIntegrationTest` written first |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify` + `preflight.sh --fix` green; Playwright TS-0014 13/13 (2 skipped, pre-existing) |
| 4 | CI green, review approved, no conflicts | yes | PR #961 merged, no conflicts |
| 5 | Deployed, smoke test passed, Issue closed | yes | Merge commit `96302f4`; smoke test above; Issue #23 closed via PR |

## Exceptions

None.
