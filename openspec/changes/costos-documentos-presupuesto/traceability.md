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
| Issue | #823 | in-progress |
| Use Case | CU-27 – Ingresar Nuevo Tipo de Documento (#180); CU-39 – Crear Plantilla Presupuesto (#192) | exists |
| Specification | `openspec/changes/costos-documentos-presupuesto/` | complete |
| Branch | `feat/823_costos-documentos-presupuesto` | active |
| Tasks | `tasks.md` | in progress |
| Commits | `364328f3`, `265713aa`, `97f20fc7`, `a5f87551` | done |
| Pull Request | pending push | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Presupuesto con un documento con costo asociado | `PagoServiceTest#shouldIncludeDocumentCostInPresupuestoTotal` | pass |
| Presupuesto con varios documentos con costo | `PagoServiceTest#shouldSumMultipleDocumentCostsInPresupuestoTotal` | pass |
| Presupuesto sin documentos con costo | `PagoServiceTest#shouldNotChangeTotalWhenNoDocumentsHaveCost` | pass |
| Definir un gasto fijo por tipo de documento | `PlantillaCostoDocumentoServiceTest#shouldAcceptFixedCostForTipoDocumento` | pass |
| Definir un gasto variable por tipo de documento | `PlantillaCostoDocumentoServiceTest#shouldAcceptVariableCostForTipoDocumento` | pass |
| Consultar los gastos por tipo de documento de una plantilla | `PlantillaCostoDocumentoControllerTest#shouldReturnCostosByTipoTramite` | pass |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU27 – Ingresar nuevo tipo de documento.md` | yes | pending commit |
| `docs/100-business/102-use-cases/CU39 – Crear Plantilla Presupuesto.md` | yes | pending commit |
| `CHANGELOG.md` | yes | pending commit |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `specs/costos-documentos-presupuesto/spec.md` |
| 2 | Failing tests written, test cases designed | yes | TDD red state confirmed before implementation |
| 3 | Suite green, coverage held, docs updated | yes | `mvn -pl backend-api verify` exit 0 post-merge |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Exceptions

None.
