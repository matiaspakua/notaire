# refactor: move ProcedureType transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-procedure-type-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.ProcedureType` built and applied `DtoProcedureType`. The persistence adapter and two domain `getDto` methods called it. This slice moves the mapping off the entity. Other entities still expose `getDto()`.

## What Changes

- Add `ProcedureTypeLegacyMapper`.
- `ProcedureTypeOpsAdapter` uses it.
- `Procedure.getDto` and `DeedManagement` copy the procedure-type fields themselves.
- Delete `getDto` and `setAttributes` from `business.ProcedureType`.
- Shrink the entity-DTO ratchet by those two lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Omitted boolean flags stay false, and an omitted version is kept | existing `setAttributes` | Made explicit |
| An invalid DTO does not change the entity | existing `setAttributes` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, adapter, two domain copies |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.ProcedureType` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/procedure/ProcedureTypeLegacyMapper.java`
- `backend-api/src/main/java/com/licensis/notaire/adapter/out/persistence/procedure/ProcedureTypeOpsAdapter.java`
- `backend-api/src/main/java/com/licensis/notaire/business/ProcedureType.java`
- `backend-api/src/main/java/com/licensis/notaire/business/Procedure.java`
- `backend-api/src/main/java/com/licensis/notaire/business/DeedManagement.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/ProcedureTypeLegacyMapperTest.java`
- `backend-api/src/test/java/com/licensis/notaire/unit/EntitiesBasicTest.java`
- `backend-api/src/test/resources/architecture/entity-dto-mapping.txt`
- `docs/openspec/changes/refactor-580-procedure-type-dto/**`

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
