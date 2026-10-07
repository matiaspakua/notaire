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
| Issue | #839 | open — partial (3/5 `protocolo-*` sub-changes merged) |
| Use Case | CU80 – Administrar Cuadernos de Folios | exists (#311) |
| Specification | `openspec/changes/protocolo-cuadernos-de-folios/` | complete |
| Branch | `feat/839_protocolo-cuadernos-de-folios` | done |
| Tasks | `tasks.md` | 37/38 complete (7.3 not literally re-verified — see note) |
| Commits | `f3482b59`, `a5053b1a`, `24c60711`, `773fd30f`, `8c4d08dc` | done |
| Pull Request | [#906](https://github.com/matiaspakua/notaire/pull/906) | merged |
| CI run | [33596715450](https://github.com/matiaspakua/notaire/actions/runs/33596715450) et al. — pre-existing failures only, see Notes | passed (merge required green checks) |
| Merge commit | `401de70a` | done (2026-09-02) |
| Release / tag | `cd.yml` ran successfully on `main` after merge (image published to GHCR) | done |
| Smoke test | `TS-0072-cuadernos-protocolo-workflow.spec.ts` passing on `main`; `GET /actuator/health` gated by `cd.yml` | done |

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
| 4 | CI green, review approved, no conflicts | yes | PR #906 merged; branch protection required green required checks |
| 5 | Deployed, smoke test passed, Issue closed | partial | Merged and deployed (`401de70a`, `cd.yml` green); Issue #839 stays open — 2 of 5 `protocolo-*` sub-changes still pending |

## Notes

- `tasks.md` 7.3 (responsive check at 320/768/1024px) was not literally re-verified this session due to a Chrome extension disconnect; the page reuses `DataTable`/`Dialog` primitives already responsive-verified elsewhere. Low risk, flagged for follow-up before merge if a browser session becomes available.
- `mvn verify -pl backend-api` surfaced 5 pre-existing failures unrelated to this change (`BusinessWorkflowIntegrationTest.createPagoReturns200`, 3x `GestionArchiveIntegrationTest`, `RemainingControllersIntegrationTest.shouldCreatePago`) plus one flaky `SimpleControllersTest` run. Confirmed identical failures present in CI run 33502758566 on `main` (commit `5c89f7b4`, 2026-09-01), predating this branch — not introduced by the cuadernos change. Reproduced identically in PR #906's CI run [33596715450](https://github.com/matiaspakua/notaire/actions/runs/33596715450).
- `Playwright E2E — Full Suite` on PR #906 reports 5 unexpected failures: `02-demo-two-full-cases.spec.ts`, `TS-0012-escritura-folio-firma.spec.ts`, `TS-0014-pagos-saldo-picker.spec.ts` (x2), `TS-0031-testimonio-generacion-verificacion-feature.spec.ts`. All 4 new `TS-0072-cuadernos-protocolo-workflow.spec.ts` scenarios pass. `02-demo-two-full-cases` and `TS-0012-escritura-folio-firma` are confirmed already failing on `main`'s last completed Playwright run ([33502528594](https://github.com/matiaspakua/notaire/actions/runs/33502528594), 2026-09-01), predating this branch. `TS-0014`/`TS-0031` did not fail in that same main run — likely pre-existing test-order/timing flakiness independent of the cuadernos change (no file this change touches is exercised by those specs) — flagged for reviewer awareness rather than treated as blocking.
- `validate-sdlc-plan.sh` (whole-repo mode, no args) fails Job `SDLC Plan Validation` on PR #906, but only due to two unrelated in-flight OpenSpec changes that pre-date this branch: `escritura-folio-picker-form` (incomplete plan, unmodified by this branch) and `persona-validacion-duplicados` (references Issue #835, since closed). `protocolo-cuadernos-de-folios` itself passes every check in isolation (`bash scripts/validate-sdlc-plan.sh protocolo-cuadernos-de-folios`).

## Exceptions

None.
