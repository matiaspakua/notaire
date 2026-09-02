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
| Issue | #839 | in-progress |
| Use Case | CU81 – Gestión de Trámites en Protocolo Auxiliar | exists (#312) |
| Specification | `openspec/changes/protocolo-auxiliar-tramites/` | complete |
| Branch | `feat/839_protocolo-auxiliar-tramites` | created |
| Tasks | `tasks.md` | Groups 1-8 complete (58/58); Groups 9-12 pending |
| Commits | `bc64a268`, `a5593aec`, `9a24dc45`, `ad014f58`, `90d12e30` | done |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Marcar un tipo de folio como auxiliar | `TipoDeFolioControllerTest#shouldMarkTipoDeFolioAsAuxiliar` | passing |
| Tipo de folio sin marcar es de Protocolo Principal | `TipoDeFolioTest#shouldDefaultEsAuxiliarToFalse` | passing |
| Hay folios auxiliares disponibles | `ProtocoloAuxiliarControllerTest#shouldListAvailableFoliosAuxiliares` | passing |
| No hay folios auxiliares disponibles | `ProtocoloAuxiliarControllerTest#shouldReturnEmptyWhenNoFoliosAuxiliaresAvailable` | passing |
| Alta de escritura de Protocolo Auxiliar con folio disponible | `ProtocoloAuxiliarControllerTest#shouldCreateEscrituraOnAvailableFolioAuxiliar` | passing |
| Intento de iniciar escritura sin folio auxiliar disponible | `ProtocoloAuxiliarControllerTest#shouldRejectEscrituraWhenNoFolioAuxiliarAvailable` | passing |
| Numeración del Protocolo Auxiliar independiente del Principal | `ProtocoloAuxiliarServiceTest#shouldKeepAuxiliarNumberingIndependentFromPrincipal` | passing |
| Escritura de Protocolo Auxiliar sin carpeta | `ProtocoloAuxiliarServiceTest#shouldNotGenerateCarpetaForAuxiliarEscritura` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU81 – Gestión de Trámites en Protocolo Auxiliar.md` | confirmed consistent, no changes needed | — |
| `CHANGELOG.md` | yes | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `tasks.md` §1 |
| 2 | Failing tests written, test cases designed | yes | `tasks.md` §3 |
| 3 | Suite green, coverage held, docs updated | yes | `tasks.md` §6-8; 5 pre-existing unrelated Playwright failures documented in 7.2 |
| 4 | CI green, review approved, no conflicts | no | pending PR |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Exceptions

None.
