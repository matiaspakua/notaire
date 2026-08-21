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
| Issue | #839 | open |
| Use Case | CU80 – Administrar Cuadernos de Folios | exists (#311) |
| Specification | `openspec/changes/protocolo-cuadernos-de-folios/` | in progress |
| Branch | `feat/839_protocolo-cuadernos-de-folios` | pending |
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
| Alta de cuaderno con rango válido | `CuadernoControllerTest#shouldCreateCuadernoFromConsecutiveFolios` | pending |
| Cantidad de folios no múltiplo de diez | `CuadernoControllerTest#shouldRejectCuadernoWhenFolioCountNotMultipleOfTen` | pending |
| Rango de folios discontinuo | `CuadernoControllerTest#shouldRejectCuadernoWithNonConsecutiveFolios` | pending |
| Folio ya asignado a otro cuaderno | `CuadernoControllerTest#shouldRejectCuadernoWithFolioAlreadyAssigned` | pending |
| Lote con folio dañado o anulado justificado | `CuadernoControllerTest#shouldCreateCuadernoWithJustifiedDamagedFolio` | pending |
| Estado de folio tras generar el cuaderno | `CuadernoControllerTest#shouldMarkFoliosAsAsignadoACuaderno` | pending |
| Primer cuaderno del año para un registro | `CuadernoControllerTest#shouldAssignNumberOneToFirstCuadernoOfYear` | pending |
| Conflicto de numeración | `CuadernoControllerTest#shouldRecalculateNextAvailableCuadernoNumber` | pending |
| Emisión de carátula de un cuaderno existente | `CuadernoControllerTest#shouldGenerateCaratulaForExistingCuaderno` | pending |
| Emisión de carátula de un cuaderno inexistente | `CuadernoControllerTest#shouldReturnNotFoundForMissingCuadernoCaratula` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU80 – Administrar Cuadernos de Folios.md` | no | — |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | no | — |
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
