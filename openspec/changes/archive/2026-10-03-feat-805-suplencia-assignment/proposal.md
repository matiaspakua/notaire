# Consult substitution on plain management notary assignment

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #805 |
| Use Case | CU22 — Registrar Suplencia; CU02 — Iniciar Gestión |
| Branch | `cursor/feat-805-suplencia-assignment-69d3` |
| Gate 1 status | passed |

## Objetivo

\#836 wired `ManagementSubstitutionService` into complete-case create/update
(and the UI toast via TS-0092), but plain `POST`/`PUT /gestiones` still set
`fkIdNotaryPerson` directly in `applyManagementRequest` with no active
substitution check. Close that residual bypass so CU22/RF-115 applies on every
notary assignment path.

## What Changes

- Call `ManagementSubstitutionService.resolveNotary` (English rename of
  `resolverNotary`) inside `applyManagementRequest` after `dateStart` is set,
  whenever a notary is supplied.
- Append the existing redirection note to management notes when a substitution
  redirects the assignment.
- Add failing-then-green integration coverage for plain POST create and plain
  PUT update during an active substitution.
- Englishize Spanish identifiers/comments/strings in touched Java
  (`ManagementSubstitutionService`, call sites, tests).
- Update CU22 permanent docs and `CHANGELOG.md` to state plain create/update
  paths honor active substitutions.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| When assigning or changing a management notary, an active substitution covering that notary for the management date MUST redirect to the substitute | CU22, CU02, RF-89 / RF-115 | Made explicit for plain POST/PUT (complete-case already covered by #836) |
| Redirection MUST be recorded in management notes (requested notary + assigned substitute) | CU22 / RF-115 | Made explicit for plain POST/PUT |
| If no active substitution covers the requested notary for the date, assignment MUST keep the requested notary | CU22, CU02 | Unchanged (existing unit tests) |

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `gestion-asignacion-suplencia`: Extend redirection requirements so plain
  `POST`/`PUT /api/v1/gestiones` (not only complete-case) consult active
  substitutions when setting `notaryPersonId`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `applyManagementRequest` + Englishize `ManagementSubstitutionService`; integration tests |
| `frontend` | no | TS-0092 already covers complete-case UI toast; no new UI for plain CRUD |
| `frontend-swing` | no | Removed; out of scope |
| `notaire-shared` | no | — |
| `infra` / observability | no | — |
| CI/CD (`.github/workflows`) | no | — |

### Surface area

- Entities: `Substitution`, `DeedManagement` — no schema change
- Endpoints: `POST /api/v1/gestiones`, `PUT /api/v1/gestiones/{id}` (behavior);
  complete-case paths remain green (regression)
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- **BREAKING:** no — clients that previously assigned a substituted notary via
  plain POST/PUT will now receive the substitute (correct RF-115 behavior)

### Architecture review

Reuses existing `ManagementSubstitutionService` already injected into
`ManagementController`. No new service, no ADR, no Flyway. Follows hexagonal
`application.usecase.management` + `adapter.in.web.management`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/100-business/102-use-cases/CU22 – Registrar Suplencia.md` | State that plain POST/PUT gestión assignment also redirects under active substitution (not only complete-case) |
| `CHANGELOG.md` | Fixed/Changed entry under `[Unreleased]` for residual plain-path bypass |
| `openspec/specs/gestion-asignacion-suplencia/spec.md` | Sync delta after merge/archive (during archive) |

## Out of Scope

- Rebuilding #836 Substitution CRUD / complete-case happy path / TS-0092 UI toast.
- Changing Substitution REST (`/api/v1/suplencia`) or entity schema.
- Renaming Spanish API path segments (`/gestiones`, `/suplencia`) — rename ADR.
- Frontend forms for plain management CRUD (if any) beyond existing E2E.
