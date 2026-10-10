# Empty or incomplete PUT bodies answer 400 (#655, testimony, procedure, workflow assignment)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU07 – Testimonios; CU53 – Trámites; CU26 – Tipos de trámite (workflow) |
| Branch | `fix/655_put_rejects_incomplete_bodies` |
| Gate 1 status | draft |

## Objetivo

The Run 8 probe found three updates that accepted an empty body `{}` and answered 200. `PUT /testimonio/{id}` overwrote the NOT NULL `number`, `observed` and `verified` columns with 0/false (data loss); `PUT /tramites/{id}` cleared the notes although `POST` requires `idProcedureType`; `PUT /tipo-tramite/{id}/workflow` silently unassigned the workflow. The Owner decided (Oct 9): reject empty or incomplete PUT bodies with 400.

## What Changes

- "Incomplete" is defined per entity from the POST rule and the NOT NULL columns the body maps to:
  - `PUT /api/v1/testimonio/{id}`: `number`, `flagged`, `verified` required (`TestimonyUpdateRequest`, which records which DTO primitives were sent). `notes` is stored as sent; an absent `deed` keeps the link.
  - `PUT /api/v1/tramites/{id}`: `idProcedureType` required, as on `POST`; `POST` now answers the standard `ErrorResponse` (`idProcedureType: es obligatorio`) instead of an ad-hoc map.
  - `PUT /api/v1/tipo-tramite/{id}/workflow`: the `workflowDefinitionId` key is required; an explicit `null` unassigns; a non-integer value answers 400 (was 500).
- `RequiredFields.check()` names every missing field in one 400 (`number: es obligatorio; flagged: es obligatorio`). Validation runs before the 404 lookup.
- OpenAPI: request schemas mark the required fields; the three PUTs document 400, and the workflow assignment also its 404/409. Six accepted oasdiff entries under #655.
- Frontend: no code change needed. The only UI caller (workflow assignment in Administración → Trámites) always sends `workflowDefinitionId` (a number or `null`); a vitest guard pins it. The testimony and procedure PUTs have no UI caller.
- Tests: `IncompletePutBodiesIntegrationTest`, `RequiredFieldsTest`, `useTiposTramite.test.tsx`, Bruno `testimonies/04a`, `procedures/04a`, `procedure-types/04a`–`04b`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An empty or incomplete PUT body answers 400 naming the missing fields | #655 (Owner decision Oct 9) | New |
| A testimony update must send number, flagged and verified | NOT NULL columns, #655 | Made explicit |
| A procedure update must send idProcedureType | POST rule, #655 | Made explicit |
| The workflow assignment must send workflowDefinitionId; null unassigns | #655 | Made explicit |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `api-error-contract`: Consistent HTTP status codes and error bodies for failed API requests.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 3 controllers, `RequiredFields`, OpenAPI, tests, Bruno |
| `frontend` | yes (test only) | vitest guard for the workflow assignment body |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `PUT /api/v1/testimonio/{id}`, `POST`/`PUT /api/v1/tramites[/{id}]`, `PUT /api/v1/tipo-tramite/{id}/workflow` (contract tightening, accepted)
- Entities / Flyway: none
- UI: none

### Architecture review

Validation stays in the web adapter, with the shared `RequiredFields` helper from #1371, so the shared `Dto*` response schemas do not change. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | Fixed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.d/655-put-rejects-incomplete-bodies.txt` | seven #655 entries (one file per PR since #1389) |

## Addendum — missing or stale version (Owner decision Oct 9)

`PUT /testimonio/{id}` copied `version` from the body into the `@Version` field: an omitted version read as 0 and a stale one hit the optimistic lock, both as 500. It now requires `version` (400) and answers 409 when it is not the stored one. Optimistic-lock failures answer 409 everywhere (`ErrorResponses.updateFailed`, `GlobalExceptionHandler`), the seven PUTs that copy `version` document 409, and the folio-type dialog, which never sent `version` (its second edit always failed), now sends the loaded row.
