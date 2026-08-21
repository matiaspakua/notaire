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
| Use Case | CU85 – Administrar Carpetas de Trámite | exists |
| Specification | `openspec/changes/protocolo-carpetas-de-tramite/` | in progress |
| Branch | `feat/839_protocolo-carpetas-de-tramite` | pending |
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
| Alta de un trámite único en la gestión | `CarpetaTramiteServiceTest#shouldGenerateCarpetaOnSingleTramiteGestion` | pending |
| Gestión que agrupa más de un trámite | `CarpetaTramiteServiceTest#shouldGenerateOneCarpetaPerTramiteInMultiTramiteGestion` | pending |
| Consulta de una carpeta existente | `CarpetaTramiteControllerTest#shouldReturnCarpetaByTramite` | pending |
| Consulta de una carpeta inexistente | `CarpetaTramiteControllerTest#shouldReturnNotFoundForMissingCarpeta` | pending |
| Carpeta puesta en espera con motivo | `CarpetaTramiteControllerTest#shouldSetCarpetaToEsperaWithMotivo` | pending |
| Intento de poner en espera sin motivo | `CarpetaTramiteControllerTest#shouldRejectEsperaWithoutMotivo` | pending |
| Archivado de gestión con todas las carpetas activas | `GestionArchiveDebtServiceTest#shouldArchiveAllActiveCarpetasOnGestionArchive` | pending |
| Archivado de gestión con una carpeta en espera sin resolver | `GestionArchiveDebtServiceTest#shouldRequireConfirmationWhenCarpetaInEsperaUnresolved` | pending |
| Confirmación explícita de archivado con carpeta en espera | `GestionArchiveDebtServiceTest#shouldArchiveCarpetaInEsperaOnExplicitConfirmation` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU85 – Administrar Carpetas de Trámite.md` | no | — |
| `docs/100-business/102-use-cases/CU16 – Archivar Gestión.md` | no | — |
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
