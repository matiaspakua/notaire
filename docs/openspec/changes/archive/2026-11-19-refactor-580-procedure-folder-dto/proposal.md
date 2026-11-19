# refactor: move procedure-folder transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-procedure-folder-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`ProcedureFolder.getDto` was called only by `ProcedureFolderOpsAdapter`. This slice moves that mapping into `ProcedureFolderLegacyMapper`.

## What Changes

- Add `ProcedureFolderLegacyMapper`.
- The ops adapter calls it.
- Delete `getDto` from the entity.
- Shrink the ratchet by `ProcedureFolder#getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A folder DTO carries id, number, status, wait reason, management id, and procedure id | existing `getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and adapter |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients.

### Write set

- `ProcedureFolderLegacyMapper.java`
- `ProcedureFolderOpsAdapter.java`
- `ProcedureFolder.java`
- `ProcedureFolderLegacyMapperTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. No new ADR. Located with `rg` because the symbol was known (`AGENTS.md` routing).

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |
| Pages | none |

## Out of Scope

- Other `getDto` entities. #580 stays open.
