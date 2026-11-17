# refactor: move FolioType transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-folio-type-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.FolioType` built and applied `DtoFolioType` on the entity. The clean `domain.folio.FolioType` record does not. This slice moves the legacy mapping into the persistence adapter and leaves the entity free of transport types. Other entities still expose `getDto()`.

## What Changes

- Add `FolioTypeLegacyMapper` with `toDto` and `apply`.
- `Folio.getDto` copies folio-type fields itself and no longer calls `FolioType.getDto()`.
- Delete `getDto` and `setAttributes` from `business.FolioType`.
- Unit tests cover the mapper and forbid those methods on the entity.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A folio type's wire fields are the same id, name, notes, enabled, auxiliary and version | CU76; #580 | Made explicit |
| The legacy folio-type entity does not import transport DTOs | CU76; #580 | Changed |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and entity methods |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.FolioType` no longer maps itself
- Endpoints: none
- Database (Flyway `V{n}`): none
- Configuration / `.env`: none
- Dependencies: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/folio/FolioTypeLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/business/FolioType.java`
- `backend-api/src/main/java/com/licensis/notaire/business/Folio.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/FolioTypeLegacyMapperTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-folio-type-dto/**`

### Architecture review

Follows ADR-030: the domain record was already free of DTOs. This slice does the same for the legacy entity that the folio aggregate still reads. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a — not user visible |

## Out of Scope

- Every other `getDto()` entity. #580 stays open.
- #577. Controllers already keep entities out of handler signatures and the loose-return baseline is empty.
- Pagination (#596).
