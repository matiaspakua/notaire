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
| Issue | #841 | in progress (implement) |
| Use Case | CU83; CU06; CU07; CU11; CU44 | exists |
| Related | #832 CLOSED (`escritura-post-firma-legal-cycle`); #833 CLOSED (`gestion-workflow-y-bitacora`); stale draft `gestion-workflow-reingreso-testimonio` superseded | referenced |
| Specification | `openspec/changes/feat-841-workflow-reingreso-loop/` | Gate 1 validated on branch |
| Branch | `cursor/feat-841-workflow-reingreso-loop-69d3` | created |
| Tasks | `tasks.md` | implement in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Generar testimonio → Testimonio Generado | WorkflowTrace / seed tests | covered |
| Ingresar a inscripción advances status | same | covered |
| Retirar → Testimonio Retirado | same | covered |
| Trace includes chronological movements | WorkflowTraceServiceTest | covered |
| Trace without testimony omits movements | WorkflowTraceServiceTest | covered |
| Reingreso appends movement | WorkflowTraceServiceTest | covered |
| Tracker shows reingreso count | Playwright workflow-tracker | covered |
| No indicator without observations | Playwright | covered |
| Degrade without post-firma nodes | Playwright / FE | covered |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | pending | — |
| `docs/200-architecture/203-design/FRONTEND-WORKFLOW-TRACKER.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | yes | WorkflowTraceServiceTest red-then-green |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None. Implement deliberately deferred (no branch/PR/push) until the coordinator
queue through `#799 → #800 → #805` clears — authorized by prep-only scope.
Strategy (b) decided in design.md (Gate 1 AC).
