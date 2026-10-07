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
| Specification | `openspec/changes/descuentos-recargos-presupuesto/` | complete |
| Branch | `feat/822_descuentos-recargos-presupuesto` | done |
| Tasks | `tasks.md` | 60/66 complete (remaining: PR/deploy/close steps) |
| Commits | `2ab2276e`, `685e5204`, `56c7ad2c`, `6446b887`, `0eebcaf9`, `840fda15` | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Crear un ítem normal sin tipo explícito | `ItemServiceTest#shouldTreatItemWithoutTypeAsNormal` | passed |
| Crear un ítem de tipo descuento | `ItemServiceTest#shouldAcceptDiscountItemWithReason` | passed |
| Crear un ítem de tipo recargo | `ItemServiceTest#shouldAcceptSurchargeItemWithReason` | passed |
| Rechazar un descuento sin motivo | `ItemServiceTest#shouldRejectDiscountItemWithoutReason` | passed |
| Rechazar un recargo sin motivo | `ItemServiceTest#shouldRejectSurchargeItemWithoutReason` | passed |
| Aceptar un ítem normal sin motivo | `ItemServiceTest#shouldAcceptNormalItemWithoutReason` | passed |
| Total con un ítem de descuento | `PagoServiceTest#shouldSubtractDiscountItemFromTotal` | passed |
| Total con un ítem de recargo | `PagoServiceTest#shouldAddSurchargeItemToTotal` | passed |
| Total sin descuentos ni recargos | `PagoServiceTest#shouldSumOnlyNormalItemsWhenNoDiscountsOrSurcharges` | passed |
| Consultar descuentos y recargos de un presupuesto con ambos | `ItemControllerTest#shouldReturnDiscountsAndSurchargesForPresupuesto` | passed |
| Consultar descuentos y recargos de un presupuesto sin ninguno | `ItemControllerTest#shouldReturnEmptyListWhenNoDiscountsOrSurcharges` | passed |
| Consultar descuentos y recargos de un presupuesto inexistente | `ItemControllerTest#shouldReturnNotFoundWhenPresupuestoDoesNotExistForReport` | passed |
| UI: tipo Descuento/Recargo exige motivo (cliente) | `items-descuentos-recargos.spec.ts#CU71-GW01/GW02` | passed |
| UI: consultar reporte de descuentos/recargos | `items-descuentos-recargos.spec.ts#CU45-GW01` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU45 – Modificar presupuesto.md` | yes | `840fda15` |
| `docs/100-business/102-use-cases/CU71 – Gestión de Items.md` | yes | `840fda15` |
| `CHANGELOG.md` | yes | `840fda15` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `proposal.md`, `specs/` |
| 2 | Failing tests written, test cases designed | yes | `ItemServiceTest`, `PagoServiceTest`, `ItemControllerTest` written first |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify`, `mvn test -Ppg-integration`, Bruno suite, full Playwright suite (417 passed, 6 pre-existing unrelated failures), docs above |
| 4 | CI green, review approved, no conflicts | pending | PR not yet opened |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
