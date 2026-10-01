# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1062 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure; CU24/CU25 – Reportes | exists |
| Specification | `openspec/changes/fix-reportes-500-1062/` | written |
| Branch | `test/1062_fix_reportes_500` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|-----------------|------|--------|
| GET /api/v1/reportes/presupuesto/{id} — 404 when id unknown | ReportesUseCaseIntegrationTest#shouldHandleBudgetEndpointGracefully | pending |
| GET /api/v1/reportes/presupuesto-inmuebles/{id} — 404 when id unknown | ReportesUseCaseIntegrationTest#shouldHandleBudgetPropertiesEndpointGracefully | pending |
| GET /api/v1/reportes/historial-gestion/{id} — 404 when id unknown | ReportesUseCaseIntegrationTest#shouldHandleHistoryManagementEndpointGracefully | pending |
| GET /api/v1/reportes/documentos-por-vencer/{id} — 404 when id unknown | ReportesUseCaseIntegrationTest#shouldHandleDocumentsPorVencerEndpointGracefully | pending |
| POST /api/v1/auth/login with null password | EdgeCaseBoundaryConditionsTest#loginWithNullPassword | pending |
| GET /api/v1/search with empty query | EdgeCaseBoundaryConditionsTest#searchEndpointHandlesEmptyQuery | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| docs/100-business/102-use-cases/CU24 – Generar libro de índices.md | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | pending |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None.
