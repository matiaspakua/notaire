# refactor: move DocumentType transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-document-type-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.DocumentType` built and applied `DtoDocumentType` on the entity. The persistence adapter already maps the JPA row to `domain.document.DocumentType` without those methods. This slice moves the leftover transport mapping into the adapter and drops the two ratchet lines.

## What Changes

- Add `DocumentTypeLegacyMapper` with `toDto` and `apply`, including the rule that a document which does not expire has no due days.
- Delete `getDto` and `setAttributes` from `business.DocumentType`.
- Shrink `entity-dto-mapping.txt`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A document type that does not expire stores no due days | existing `setAttributes` | Made explicit |
| The legacy document-type entity does not import transport DTOs | CU76; #580 | Changed |

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

- Entities: `business.DocumentType` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/document/DocumentTypeLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/business/DocumentType.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/DocumentTypeLegacyMapperTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-document-type-dto/**`

### Architecture review

Follows ADR-030. The live catalog already uses `domain.document.DocumentType`. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Out of Scope

- Every other `getDto()` entity. #580 stays open.
- Pagination (#596).
