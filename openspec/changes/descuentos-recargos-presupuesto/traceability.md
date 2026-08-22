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
| Issue | #822 | open (in-progress) |
| Use Case | CU-45 – Modificar Presupuesto (#198); CU-71 – Gestión de Items (#300) | exists |
| Specification | `openspec/changes/descuentos-recargos-presupuesto/` | in progress |
| Branch | `feat/822_descuentos-recargos-presupuesto` | pending |
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
| Crear un ítem normal sin tipo explícito | `ItemServiceTest#shouldTreatItemWithoutTypeAsNormal` | pending |
| Crear un ítem de tipo descuento | `ItemServiceTest#shouldAcceptDiscountItemWithReason` | pending |
| Crear un ítem de tipo recargo | `ItemServiceTest#shouldAcceptSurchargeItemWithReason` | pending |
| Rechazar un descuento sin motivo | `ItemServiceTest#shouldRejectDiscountItemWithoutReason` | pending |
| Rechazar un recargo sin motivo | `ItemServiceTest#shouldRejectSurchargeItemWithoutReason` | pending |
| Aceptar un ítem normal sin motivo | `ItemServiceTest#shouldAcceptNormalItemWithoutReason` | pending |
| Total con un ítem de descuento | `PagoServiceTest#shouldSubtractDiscountItemFromTotal` | pending |
| Total con un ítem de recargo | `PagoServiceTest#shouldAddSurchargeItemToTotal` | pending |
| Total sin descuentos ni recargos | `PagoServiceTest#shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges` | pending |
| Consultar descuentos y recargos de un presupuesto con ambos | `ItemControllerTest#shouldReturnDiscountsAndSurchargesForPresupuesto` | pending |
| Consultar descuentos y recargos de un presupuesto sin ninguno | `ItemControllerTest#shouldReturnEmptyListWhenNoDiscountsOrSurcharges` | pending |
| Consultar descuentos y recargos de un presupuesto inexistente | `ItemControllerTest#shouldReturnNotFoundWhenPresupuestoDoesNotExistForReport` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU45 – Modificar Presupuesto.md` | no | — |
| `docs/100-business/102-use-cases/CU71 – Gestión de Items.md` | no | — |
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
