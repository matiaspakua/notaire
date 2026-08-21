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
| Use Case | CU81 – Gestión de Trámites en Protocolo Auxiliar | exists (#312) |
| Specification | `openspec/changes/protocolo-auxiliar-tramites/` | in progress |
| Branch | `feat/839_protocolo-auxiliar-tramites` | pending |
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
| Marcar un tipo de folio como auxiliar | `TipoDeFolioControllerTest#shouldMarkTipoDeFolioAsAuxiliar` | pending |
| Tipo de folio sin marcar es de Protocolo Principal | `TipoDeFolioTest#shouldDefaultEsAuxiliarToFalse` | pending |
| Hay folios auxiliares disponibles | `ProtocoloAuxiliarControllerTest#shouldListAvailableFoliosAuxiliares` | pending |
| No hay folios auxiliares disponibles | `ProtocoloAuxiliarControllerTest#shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable` | pending |
| Alta de escritura de Protocolo Auxiliar con folio disponible | `ProtocoloAuxiliarControllerTest#shouldCreateEscrituraOnAvailableFolioAuxiliar` | pending |
| Intento de iniciar escritura sin folio auxiliar disponible | `ProtocoloAuxiliarControllerTest#shouldRejectEscrituraWhenNoFolioAuxiliarAvailable` | pending |
| Numeración del Protocolo Auxiliar independiente del Principal | `ProtocoloAuxiliarServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` | pending |
| Escritura de Protocolo Auxiliar sin carpeta | `ProtocoloAuxiliarServiceTest#shouldNotGenerateCarpetaForAuxiliarEscritura` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU81 – Gestión de Trámites en Protocolo Auxiliar.md` | no | — |
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
