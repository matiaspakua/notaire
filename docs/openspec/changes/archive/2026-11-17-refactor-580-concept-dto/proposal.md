# refactor: move Concept transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-concept-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.Concept` built and applied `DtoConcept`. `ConceptOpsAdapter` called those methods. This slice moves the mapping into the persistence adapter. Other entities still expose `getDto()`.

## What Changes

- Add `ConceptLegacyMapper` with `toDto` and `apply`.
- `ConceptOpsAdapter` uses the mapper.
- Delete `getDto` and `setAttributes` from `business.Concept`.
- Shrink the entity-DTO ratchet by those two lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A concept DTO carries id, name, value, percentage, version, enabled and fixed | existing `getDto` | Made explicit |
| Null name, value and percentage on update do not clear the stored field | existing `setAttributes` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, adapter, entity |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.Concept` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/concept/ConceptLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/concept/ConceptOpsAdapter.java`
- `backend-api/src/main/java/com/licensis/notaire/business/Concept.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ConceptDtoMappingTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-concept-dto/**`

### Architecture review

Follows ADR-030. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Every other `getDto()` entity. #580 stays open.
- Pagination (#596).
