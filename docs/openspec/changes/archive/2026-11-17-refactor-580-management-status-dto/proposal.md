# refactor: move ManagementStatus transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-management-status-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.ManagementStatus` built and applied `DtoManagementStatus`. `History.getDto` called it. This slice moves the mapping off the entity. Other entities still expose `getDto()`.

## What Changes

- Add `ManagementStatusLegacyMapper` with `toDto` and `apply`.
- `History.getDto` copies the status fields itself.
- Delete `getDto` and `setAttribute` from `business.ManagementStatus`.
- Shrink the entity-DTO ratchet by those two lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| An invalid status DTO is rejected and does not change the entity | existing `setAttribute` | Made explicit |
| A history DTO still carries the status id, name, notes and version | existing `History.getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and history copy |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.ManagementStatus` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/management/ManagementStatusLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/business/ManagementStatus.java`
- `backend-api/src/main/java/com/licensis/notaire/business/History.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ManagementStatusLegacyMapperTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-management-status-dto/**`

### Architecture review

Follows ADR-030. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Removing `getDto` from `History`. #580 stays open.
- Pagination (#596).
