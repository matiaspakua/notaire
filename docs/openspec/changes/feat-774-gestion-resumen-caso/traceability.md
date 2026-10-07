# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #774 | open → in progress |
| Use Case | CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio (#165); CU15 – Procesar pago (#168); CU70 – Gestión de Copias (#243) | exists |
| Related | #771 (parent), #772, #773, #832, #841, #851 | referenced |
| Specification | `docs/openspec/changes/feat-774-gestion-resumen-caso/` | Gate 1 draft |
| Branch | `feat/774_gestion_workflow_links` | created from updated `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A gestión with a deed, a testimony, a movement and a copy | `ManagementCaseSummaryServiceTest` | pending |
| A testimony without movements | `ManagementCaseSummaryServiceTest` | pending |
| A withdrawn testimony | `ManagementCaseSummaryServiceTest` | pending |
| No deed yet | `ManagementCaseSummaryIntegrationTest` | pending |
| Unknown id | `ManagementCaseSummaryIntegrationTest` | pending |
| Open the summary | `testing/e2e/tests/TS-0098-gestion-resumen-caso-feature.spec.ts` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | — |
| `backend-api/openapi/openapi.yaml` | pending | — |
| `docs/300-development/303-testing/CU-API-MATRIX.csv` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | `bash scripts/validate-sdlc-plan.sh` |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
