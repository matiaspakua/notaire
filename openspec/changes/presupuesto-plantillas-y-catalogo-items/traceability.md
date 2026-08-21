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
| Issue | #834 | open |
| Use Case | CU39 – Crear Plantilla Presupuesto (#192); CU55 – Modificar Plantilla Presupuesto (#208); CU49 – Eliminar Plantilla Presupuesto (#202); CU71 – Gestión de Items (#300) | exists |
| Specification | `openspec/changes/presupuesto-plantillas-y-catalogo-items/` | in progress |
| Branch | `feat/834_presupuesto-plantillas-y-catalogo-items` | not created |
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
| Carga exitosa desde una plantilla existente | `PresupuestoPlantillaServiceTest#shouldLoadItemsFromPlantilla` | pending |
| Ítems cargados no se recalculan si la plantilla cambia después | `PresupuestoPlantillaServiceTest#shouldNotRecalculateLoadedItemsWhenPlantillaChanges` | pending |
| Rechazo cuando el tipo de trámite no tiene plantilla | `PresupuestoPlantillaServiceTest#shouldRejectWhenNoPlantillaForTipoTramite` | pending |
| Agregado exitoso de un ítem del catálogo | `PresupuestoCatalogoItemsServiceTest#shouldAddSingleCatalogItem` | pending |
| Agregado de varios ítems del catálogo en una sola operación | `PresupuestoCatalogoItemsServiceTest#shouldAddMultipleCatalogItems` | pending |
| Rechazo al referenciar un ítem de catálogo inexistente | `PresupuestoCatalogoItemsServiceTest#shouldRejectUnknownCatalogItem` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU39 – Crear Plantilla Presupuesto.md` | no | pending |
| `docs/100-business/102-use-cases/CU71 – Gestión de Items.md` | no | pending |
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
