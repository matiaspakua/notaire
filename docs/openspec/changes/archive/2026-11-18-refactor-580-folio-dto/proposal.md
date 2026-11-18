# refactor: move Folio transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-folio-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.Folio` built `DtoFolio`. `DeedLegacyMapper` and `Testimony.copyDeed` called `folio.getDto()`. This slice moves that mapping off the entity. The nested deed stays a short copy, so mapping a folio does not walk the deed's folio list.

## What Changes

- Add `FolioLegacyMapper`.
- `DeedLegacyMapper` calls it.
- `Testimony.copyDeed` copies the folio fields itself.
- Delete `getDto` from `business.Folio`.
- Shrink the ratchet by `Folio#getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A folio DTO carries id, number, year, status, notes, version, notary, folio type, and a short deed | existing `getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and two callers |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD | no | |

### Surface area

- Entities: `business.Folio` no longer maps itself
- Endpoints: none
- Not **BREAKING** for API clients.

### Write set

- `FolioLegacyMapper.java`
- `Folio.java`, `Testimony.java`, `DeedLegacyMapper.java`
- `FolioLegacyMapperTest.java`, `FolioTest.java`, `EntitiesBasicTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |

## Out of Scope

- Other `getDto` entities. #580 stays open.
- Paging users or procedure folders.
