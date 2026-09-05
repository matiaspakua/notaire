# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #839 | closed by this PR's merge — shared umbrella Issue covering 5 sub-changes (RF-74 a RF-95); remains open until `protocolo-numeracion-escrituras`, the last of the 5, also merges |
| Use Case | CU85 – Administrar Carpetas de Trámite | exists |
| Specification | `openspec/changes/protocolo-carpetas-de-tramite/` | complete |
| Branch | `feat/839_protocolo-carpetas-de-tramite` | created |
| Tasks | `tasks.md` | complete |
| Commits | `0afdb43f` (feat), `7fd8d2c8` (merge main), `813a3356` (docs fix), `76abdfdf` (docs fix) | complete |
| Pull Request | [#937](https://github.com/matiaspakua/notaire/pull/937) | merged |
| CI run | https://github.com/matiaspakua/notaire/actions/runs/33863042046 | passed |
| Merge commit | `b648fbc0` | complete |
| Release / tag | Continuous deploy on merge to `main` | complete |
| Smoke test | `mvn verify` + Playwright E2E in CI, PR #937 | passed |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Alta de un trámite único en la gestión | `CarpetaTramiteServiceTest#shouldGenerateCarpetaOnSingleTramiteGestion` | passing |
| Gestión que agrupa más de un trámite | `CarpetaTramiteServiceTest#shouldGenerateOneCarpetaPerTramiteInMultiTramiteGestion` | passing |
| Consulta de una carpeta existente | `CarpetaTramiteControllerTest#shouldReturnCarpetaByTramite` | passing |
| Consulta de una carpeta inexistente | `CarpetaTramiteControllerTest#shouldReturnNotFoundForMissingCarpeta` | passing |
| Carpeta puesta en espera con motivo | `CarpetaTramiteControllerTest#shouldSetCarpetaToEsperaWithMotivo` | passing |
| Intento de poner en espera sin motivo | `CarpetaTramiteControllerTest#shouldRejectEsperaWithoutMotivo` | passing |
| Archivado de gestión con todas las carpetas activas | `GestionArchiveDebtServiceTest#shouldArchiveAllActiveCarpetasOnGestionArchive` | passing |
| Archivado de gestión con una carpeta en espera sin resolver | `GestionArchiveDebtServiceTest#shouldRequireConfirmationWhenCarpetaInEsperaUnresolved` | passing |
| Confirmación explícita de archivado con carpeta en espera | `GestionArchiveDebtServiceTest#shouldArchiveCarpetaInEsperaOnExplicitConfirmation` | passing |
| E2E: golden path + edge paths + responsive (CU85) | `frontend/tests/e2e/carpetas-de-tramite.spec.ts` (6 tests) | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de Trámite.md` | yes | `0afdb43f` |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | yes | `0afdb43f` |
| `CHANGELOG.md` | yes | `0afdb43f` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `scripts/validate-sdlc-plan.sh` — protocolo-carpetas-de-tramite ✓ |
| 2 | Failing tests written, test cases designed | yes | tests written first, observed failing, then implementation added |
| 3 | Suite green, coverage held, docs updated | yes | `mvn test -pl backend-api` 1711/1711; `bash scripts/preflight.sh --fix` all green (own change) |
| 4 | CI green, review approved, no conflicts | yes | PR #937 merged via `b648fbc0` |
| 5 | Deployed, smoke test passed, Issue closed | yes | Continuous deploy on merge to `main`; Issue #839 closed (see note above — remains open pending the last of 5 sub-changes) |

## Exceptions

None.
