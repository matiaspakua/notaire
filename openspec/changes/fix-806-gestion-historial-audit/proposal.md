# Close residual gestión History write gaps

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #806 |
| Use Case | CU13 — Ver historial de gestión; CU02 — Iniciar Gestión; CU53 — Modificar Gestión |
| Branch | `cursor/fix-806-gestion-historial-audit-69d3` |
| Gate 1 status | passed |

## Objetivo

Issue #833 wired History (bitácora) for complete-case create, `/transition`, and
archive, but plain `POST`/`PUT /gestiones` and `PUT .../complete-case` status
changes still skip History, and `GET /{id}/estado-actual` 404s when History is
empty. Close those residual gaps so CU13 reflects every status write path.

## What Changes

- Call `ManagementBitacoraService.registerStatus` when plain create/update sets
  or changes status, and when complete-case update changes status (compare
  previous vs new status id).
- Do not invent a History row when plain create has no status.
- `GET /gestiones/{id}/estado-actual`: if History is empty, synthesize a summary
  from `DeedManagement.fkIdManagementStatus` (management id + as-of-now) → 200;
  missing entity → 404; null status → 404.
- Confirm gestiones UI bitácora (`useHistorial`, TS-0028) already covers CU13.
- Update CU13 permanent docs and `CHANGELOG.md`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Every status assignment or change on a gestión MUST append a History row | CU13, RF-110, CU02, CU53 | Made explicit for orphan write paths |
| Plain create without a status MUST NOT invent a History row | CU13 / #806 | New (edge) |
| Current-status query MUST return entity status when History is empty | CU13, CU14/RF-24 | New (fallback) |
| Missing gestión or null status on fallback MUST yield 404 | CU13 | Made explicit |
| History writes MUST go through the existing bitácora port/service (not AuditoriaAspect) | CU13 / #833 | Made explicit |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `gestion-bitacora`: Extend write coverage to plain create/update and
  complete-case update status changes; document `estado-actual` entity-status
  fallback when History is empty.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `ManagementController` orphan write paths + `estado-actual` fallback; integration tests |
| `frontend` | no | Bitácora UI already present (TS-0028 / `useHistorial`) — confirm only |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | no | Reuse `DtoHistorySummary` |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `History`, `DeedManagement` — no schema change
- Endpoints: `POST /api/v1/gestiones`, `PUT /api/v1/gestiones/{id}`,
  `PUT /api/v1/gestiones/{id}/complete-case`, `GET /api/v1/gestiones/{id}/estado-actual`
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** no — `estado-actual` becomes more available (200 instead of 404
  when entity has status but History empty)

### Architecture review

Follows existing hexagonal use of `ManagementBitacoraService` /
`ManagementBitacoraPort`. No second writer. No ADR required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU13 – Ver historial de gestión.md` | Document that status changes on plain create/update and complete-case update write History; document `estado-actual` entity-status fallback |
| `CHANGELOG.md` | Fixed entry under `[Unreleased]` for residual History gaps |
| `backend-api/api-test/COVERAGE.md` | Note estado-actual fallback if Bruno/docs assert 404-on-empty |

## Out of Scope

- Rebuilding #833 happy paths (complete-case create, `/transition`, archive).
- Workflow transition validation for plain PUT status changes (#804 / F6).
- Backfilling History rows for legacy gestiones (read-time fallback only).
- Renaming Spanish API path segments (`/historial`, `/estado-actual`) — deferred
  to rename ADR if any.
