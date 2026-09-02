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
| Specification | `openspec/changes/protocolo-cuadernos-de-folios/` | complete |
| Branch | `feat/839_protocolo-cuadernos-de-folios` | done |
| Tasks | `tasks.md` | 33/38 complete (7.3 partially blocked — see note) |
| Commits | `f3482b59`, `a5053b1a`, `24c60711` | done |
| Pull Request | [#906](https://github.com/matiaspakua/notaire/pull/906) | open |
| CI run | pending on PR #906 | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta de cuaderno con rango válido | `CuadernoControllerTest#shouldCreateCuadernoFromConsecutiveFolios` | passed |
| Cantidad de folios no múltiplo de diez | `CuadernoControllerTest#shouldRejectCuadernoWhenFolioCountNotMultipleOfTen` | passed |
| Rango de folios discontinuo | `CuadernoControllerTest#shouldRejectCuadernoWithNonConsecutiveFolios` | passed |
| Folio ya asignado a otro cuaderno | `CuadernoControllerTest#shouldRejectCuadernoWithFolioAlreadyAssigned` | passed |
| Lote con folio dañado o anulado justificado | `CuadernoControllerTest#shouldCreateCuadernoWithJustifiedDamagedFolio` | passed |
| Estado de folio tras generar el cuaderno | `CuadernoControllerTest#shouldMarkFoliosAsAsignadoACuaderno` | passed |
| Primer cuaderno del año para un registro | `CuadernoControllerTest#shouldAssignNumberOneToFirstCuadernoOfYear` | passed |
| Conflicto de numeración | `CuadernoControllerTest#shouldRecalculateNextAvailableCuadernoNumber` | passed |
| Emisión de carátula de un cuaderno existente | `CuadernoControllerTest#shouldGenerateCaratulaForExistingCuaderno` | passed |
| Emisión de carátula de un cuaderno inexistente | `CuadernoControllerTest#shouldReturnNotFoundForMissingCuadernoCaratula` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU80 – Administrar Cuadernos de Folios.md` | confirmed accurate, no change needed | — |
| `docs/100-business/102-use-cases/CU28 – Ingresar nuevos folios.md` | yes | `24c60711` |
| `CHANGELOG.md` | yes | `24c60711` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #839, `specs/cuadernos-de-folios/spec.md` |
| 2 | Failing tests written, test cases designed | yes | `CuadernoTest`, `CuadernoControllerTest` observed failing pre-implementation |
| 3 | Suite green, coverage held, docs updated | yes | `mvn verify`, `bash testing/scripts/test.sh`, `npx playwright test` all green for this change (5 pre-existing unrelated Pago/Gestion failures confirmed present on `main` before this branch); docs updated in `24c60711` |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending merge |

## Notes

- `tasks.md` 7.3 (responsive check at 320/768/1024px) was not literally re-verified this session due to a Chrome extension disconnect; the page reuses `DataTable`/`Dialog` primitives already responsive-verified elsewhere. Low risk, flagged for follow-up before merge if a browser session becomes available.
- `mvn verify -pl backend-api` surfaced 5 pre-existing failures unrelated to this change (`BusinessWorkflowIntegrationTest.createPagoReturns200`, 3x `GestionArchiveIntegrationTest`, `RemainingControllersIntegrationTest.shouldCreatePago`) plus one flaky `SimpleControllersTest` run. Confirmed identical failures present in CI run 33502758566 on `main` (commit `5c89f7b4`, 2026-09-01), predating this branch — not introduced by the cuadernos change.

## Exceptions

None.
