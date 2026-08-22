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
| Issue | #823 | open (in-progress) |
| Use Case | CU-27 – Ingresar Nuevo Tipo de Documento (#180); CU-39 – Crear Plantilla Presupuesto (#192) | exists |
| Specification | `openspec/changes/costos-documentos-presupuesto/` | in progress |
| Branch | `feat/823_costos-documentos-presupuesto` | pending |
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
| Presupuesto con un documento con costo asociado | `PagoServiceTest#shouldIncludeDocumentCostInPresupuestoTotal` | pending |
| Presupuesto con varios documentos con costo | `PagoServiceTest#shouldSumMultipleDocumentCostsInPresupuestoTotal` | pending |
| Presupuesto sin documentos con costo | `PagoServiceTest#shouldNotChangeTotalWhenNoDocumentsHaveCost` | pending |
| Definir un gasto fijo por tipo de documento | `PlantillaCostoDocumentoServiceTest#shouldAcceptFixedCostForTipoDocumento` | pending |
| Definir un gasto variable por tipo de documento | `PlantillaCostoDocumentoServiceTest#shouldAcceptVariableCostForTipoDocumento` | pending |
| Consultar los gastos por tipo de documento de una plantilla | `PlantillaCostoDocumentoControllerTest#shouldReturnCostosByTipoTramite` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU27 – Ingresar Nuevo Tipo de Documento.md` | no | — |
| `docs/100-business/102-use-cases/CU39 – Crear Plantilla Presupuesto.md` | no | — |
| `CHANGELOG.md` | no | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | no | in progress |
| 2 | Failing tests written, test cases designed | no | — |
| 3 | Suite green, coverage held, docs updated | no | — |
| 4 | CI green, review approved, no conflicts | no | — |
| 5 | Deployed, smoke test passed, Issue closed | no | — |

## Exceptions

None.
