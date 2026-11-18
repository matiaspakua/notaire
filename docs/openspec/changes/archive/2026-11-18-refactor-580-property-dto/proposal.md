# refactor: move Property transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-property-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.Property` built `DtoProperty`. `Procedure.getDto` and `DeedManagement` called it. This slice moves the mapping off the entity. Other entities still expose `getDto()`.

## What Changes

- Add `PropertyLegacyMapper`.
- `Procedure.getDto` and `DeedManagement` copy the property fields themselves.
- Delete `getDto` from `business.Property`.
- Shrink the entity-DTO ratchet by `Property#getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A nested property DTO carries id, address, cadastral designation, notes, appraisal, registration, volume/folio and boundaries | existing `getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and two domain copies |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.Property` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/property/PropertyLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/business/Property.java`
- `backend-api/src/main/java/com/licensis/notaire/business/Procedure.java`
- `backend-api/src/main/java/com/licensis/notaire/business/DeedManagement.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/PropertyLegacyMapperTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-property-dto/**`

### Architecture review

Follows ADR-030. No new ADR. Located with repolens search before the first edit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Every other `getDto()` entity. #580 stays open.
- Pagination (#596).
