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
| Issue | #841 | open |
| Use Case | CU83 – Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU06 – Firmar escritura (#159); CU07 – Generar testimonio (#160); CU11 – Ingresar para inscripción (#164); CU44 – Reingresar testimonio (#197) | exists |
| Specification | `openspec/changes/gestion-workflow-reingreso-testimonio/specs/workflow-testimonio-movimiento-tracker/spec.md` | done |
| Branch | `feat/841_gestion-workflow-reingreso-testimonio` | pending (not yet created) |
| Tasks | `tasks.md` | 0/69 complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Generar el testimonio de una escritura firmada avanza el estado de la gestión | `WorkflowNodeRepositoryIntegrationTest#shouldExposeTestimonioGeneradoNodeInStandardWorkflow` (or `WorkflowSeedDataIntegrationTest`) | pending |
| Ingresar el testimonio a inscripción avanza el estado de la gestión | same class, transition-level assertion | pending |
| Retirar el testimonio avanza el estado de la gestión | same class, transition-level assertion | pending |
| Gestión con testimonio en curso incluye sus movimientos en el trace | `WorkflowTraceServiceTest#shouldIncludeMovimientosTestimonioWhenTestimonioHasMovements` | pending |
| Gestión sin testimonio no incluye movimientos | `WorkflowTraceServiceTest#shouldNotIncludeMovimientosWhenNoTestimonio` | pending |
| Un reingreso agrega un nuevo movimiento sin perder los anteriores | `WorkflowTraceServiceTest#shouldIncludeAllMovimientosInChronologicalOrder` | pending |
| Testimonio con reingresos muestra el conteo en el nodo de inscripción | `frontend/tests/e2e/workflow-tracker.spec.ts` — "shows reingreso count on inscripción node" | pending |
| Testimonio sin reingresos no muestra el indicador | `frontend/tests/e2e/workflow-tracker.spec.ts` — "does not show reingreso indicator without observations" | pending |
| Gestión cuyo tipo de trámite no tiene el workflow post-firma configurado | `WorkflowTraceServiceTest#shouldDegradeGracefullyWhenPostFirmaNodesMissing`; `workflow-tracker.spec.ts` — "renders without the post-firma nodes when not configured" | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | no | — |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | Issue #841 open; `proposal.md` and `specs/workflow-testimonio-movimiento-tracker/spec.md` written; `design.md`/`tasks.md` still pending |
| 2 | Failing tests written, test cases designed | no | — |
| 3 | Suite green, coverage held, docs updated | no | — |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.
