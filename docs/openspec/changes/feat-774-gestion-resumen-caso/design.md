> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates, §7 Testing Rules, §11 Release Rules.

## Context

Issue #774, Use Case CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU12 – Retirar testimonio (#165); CU15 – Procesar pago (#168); CU70 – Gestión de Copias (#243). #772 captures case dependencies at creation and the post-signing modules exist (#832, #841, #851); #774 asks for them to be visible from the gestión.

## Goals / Non-Goals

**Goals:** One place to see how far a case has progressed after signing.
**Non-Goals:** Creating deeds, folios, testimonies, copies or payments from inside the gestión screen; changing the workflow engine.

## Decisions

1. The summary is a read endpoint of its own instead of enlarging workflow-trace, which is tied to a workflow definition and fails for gestiones without one. Rejected: adding the data to the trace.
2. Pagos reuse the existing resumen-financiero endpoint from the frontend instead of duplicating the totals in the new DTO.

## Riesgos / Trade-offs

- Navigating deed to testimonies to copies loads lazy collections; the service is read-only transactional and a gestión has few deeds.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| A gestión with a deed, a testimony, a movement and a copy | unit | `ManagementCaseSummaryServiceTest` |
| A testimony without movements | unit | `ManagementCaseSummaryServiceTest` |
| A withdrawn testimony | unit | `ManagementCaseSummaryServiceTest` |
| No deed yet | integration | `ManagementCaseSummaryIntegrationTest` |
| Unknown id | integration | `ManagementCaseSummaryIntegrationTest` |
| Open the summary | e2e | `testing/e2e/tests/TS-0098-gestion-resumen-caso-feature.spec.ts` |

- New unit tests (`src/test/java/.../unit/`): `ManagementCaseSummaryServiceTest`, `useResumenCaso` hook test
- New integration tests: `ManagementCaseSummaryIntegrationTest` (H2)
- Coverage impact: positive: new service covered; the floor is unchanged

## Regression Strategy

- Existing tests affected: none; new classes
- Full suite command: `mvn verify -pl backend-api; npx vitest run; Playwright TS-0098`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: none

## Playwright Strategy

No UI change; n/a.

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: single PR
- Configuration or `.env` keys: none
- Feature flag: no
- Smoke test after deploy (Gate 5): CI green on the merge commit; the case summary opens from the gestiones screen

## Rollback Strategy

- Revert the PR.
