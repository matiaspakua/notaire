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
| Issue | #833 | open |
| Use Case | CU13 – Ver historial de gestión (#166); CU83 – Definir Workflow de Estados y Transiciones (#451, #453, #454, #455); CU16 – Archivar Gestión (#169, #819) | exists |
| Specification | `openspec/changes/gestion-workflow-y-bitacora/` | in progress |
| Branch | `feat/833_gestion-workflow-y-bitacora` | not created |
| Tasks | `tasks.md` | 0/N complete |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Transición válida se aplica | `GestionTransitionServiceTest#shouldApplyValidTransition`, `GestionTransitionControllerIntegrationTest#shouldApplyValidTransition` | passing |
| Transición inválida es rechazada | `GestionTransitionServiceTest#shouldRejectInvalidTransition`, `GestionTransitionControllerIntegrationTest#shouldRejectInvalidTransition` | passing |
| Gestión sin workflow definido rechaza cualquier transición | `GestionTransitionServiceTest#shouldRejectTransitionWhenNoWorkflowDefinition`, `GestionTransitionControllerIntegrationTest#shouldRejectTransitionWhenNoWorkflowDefinition` | passing |
| Alta de gestión registra su estado inicial | `GestionBitacoraServiceTest#shouldRecordHistorialOnCreate` | passing |
| Transición válida registra el nuevo estado | `GestionBitacoraServiceTest#shouldRecordHistorialOnValidTransition` | passing |
| Archivado registra el estado archivado | `GestionBitacoraServiceTest#shouldRecordHistorialOnArchive` | passing |
| Consulta devuelve el historial completo ordenado | `GestionBitacoraServiceTest#shouldReturnOrderedHistorial`, `GestionBitacoraControllerIntegrationTest#shouldReturnOrderedHistorial` | passing |
| Archiving succeeds when the transition to Archivada is valid | `GestionArchiveDebtServiceTest#shouldArchiveWhenTransitionValid` | passing |
| Archiving is rejected when the transition to Archivada is invalid | `GestionArchiveDebtServiceTest#shouldRejectArchiveWhenTransitionInvalid` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | no | pending |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | no | pending |
| `docs/100-business/102-use-cases/CU83 – Definir Workflow de Estados y Transiciones.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | — |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
