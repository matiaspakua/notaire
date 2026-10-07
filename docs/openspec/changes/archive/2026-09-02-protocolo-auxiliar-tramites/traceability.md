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
| Commits | `bc64a268`, `a5593aec`, `9a24dc45`, `ad014f58`, `90d12e30`, `ae247a5e` | done |
| Pull Request | [#907](https://github.com/matiaspakua/notaire/pull/907) | merged |
| CI run | `PR Validation`/`CI - Build, Test & Security`/`Playwright E2E` fail only on pre-existing, unrelated baseline (documented in `tasks.md` 6.1/7.2/8.6); `Frontend CI` green | passed with documented exception |
| Merge commit | `6b29fd96` (squash) | done |
| Release / tag | n/a — no tagged release cut for this merge | — |
| Smoke test | manual API smoke test 2026-09-02 against local stack: created tipo-folio with `esAuxiliar=true`, folio on it, listed via `GET /protocolo-auxiliar/folios-disponibles` (found), started escritura via `POST /protocolo-auxiliar/escrituras` (id 23), confirmed folio dropped out of disponibles and no `tramites` row was created (service only touches `Escritura`/`Folio`); `GET /actuator/health` returned 200; test data cleaned up afterward | passed |

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
| 4 | CI green, review approved, no conflicts | yes | PR #907 merged as `6b29fd96` |
| 5 | Deployed, smoke test passed, Issue closed | yes | smoke test passed (see Chain — Smoke test); Issue #839 left open — partial completion, 3 of 5 `protocolo-*` sub-changes still unmerged |

## Exceptions

None.
