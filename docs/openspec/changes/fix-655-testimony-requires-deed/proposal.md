# A bare testimony is created as the copy of an existing deed (slice of #655)

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #655 |
| Use Case | CU07 – Generar Testimonio; CU08 – Verificar Testimonio |
| Branch | `fix/655_testimony_requires_deed` |
| Gate 1 status | draft |

## Objetivo

`POST /api/v1/testimonio` stored a testimony without any deed (`{}` answered 201), although a testimony is by definition the certified copy of a deed. The Owner decided the bare create requires the deed (#655, Run 7), rather than removing the endpoint.

## What Changes

- `TestimonyController.create` takes `TestimonyCreateRequest` (extends `DtoTestimony`, same fields) whose `deed` is `@NotNull` and documented required; a deed without `idDeed` answers 400, an unknown `idDeed` 404, before anything is saved. `PUT` keeps `DtoTestimony` and stays as it was.
- `openapi.yaml` regenerated; `accepted-breaking-changes.txt`: one entry for the new break; the three entries of #1332/#1333/#1334, already on `main`, removed (stale-entry check).
- Tests: `TestimonyCreateRequiresDeedIntegrationTest`, `SimpleControllersTest` updates, Bruno `testimonies/00, 09, 10` and `testimony-movements/00-setup-deed, 09-teardown-deed`; CHANGELOG entry.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A bare testimony references an existing deed | CU07, #655 (Owner decision) | New |

## Capabilities

### New Capabilities

- (none)

### Modified Capabilities

- `testimonies`: Generating, verifying and tracking testimonies (certified copies of deeds).

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | `TestimonyController`, OpenAPI, tests, Bruno |
| `frontend` | no | the UI generates testimonies through `/testimonio/generar` |
| `testing` | no | Playwright helpers already send the deed |
| Docs / scripts / CI | yes | CHANGELOG, accepted-breaking-changes |

### Surface area

- Endpoints: `POST /api/v1/testimonio` (`deed` required; 400 without `idDeed`; 404 for an unknown deed; breaking, accepted)
- Entities / Flyway: none
- UI: none

### Architecture review

Bean validation on a create-only subclass of the existing DTO, so the request shape and every field name stay the same and only `deed` becomes required. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | BREAKING Changed entry |
| `backend-api/openapi/openapi.yaml` | regenerated |
| `backend-api/openapi/accepted-breaking-changes.txt` | one entry, three stale ones removed |
