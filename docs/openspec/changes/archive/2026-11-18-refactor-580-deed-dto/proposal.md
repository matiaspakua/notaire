# refactor: move Deed transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-deed-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`business.Deed` built `DtoDeed`, including nested folios and procedures. `Testimony.getDto` called `fkIdDeed.getDto()`. This slice moves that mapping off the deed entity.

## What Changes

- Add `DeedLegacyMapper`.
- `Testimony.getDto` copies the deed fields itself, so `business` does not depend on the adapter.
- `DeedEntityTest` calls the mapper.
- Delete `getDto` from `business.Deed`.
- Shrink the entity-DTO ratchet by `Deed#getDto`.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A deed DTO carries id, number, body, status, dates, registration entry, version, nested procedures, and the same folio side effect as before | existing `getDto` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `.openspec.yaml` sets `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, Testimony copy, Deed entity |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` / observability | no | |
| CI/CD (`.github/workflows`) | no | |

### Surface area

- Entities: `business.Deed` no longer maps itself
- Endpoints: none
- Database: none
- Not **BREAKING** for API clients.

### Write set

- `DeedLegacyMapper.java`
- `business/Deed.java`
- `business/Testimony.java`
- `DeedLegacyMapperTest.java`
- `DeedEntityTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. No new ADR. Located with a paired grep and repolens search before the first edit.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| None beyond the code | The HTTP API is unchanged |
| `CHANGELOG.md` | n/a |

## Out of Scope

- `getDto` on Testimony, Folio, Procedure, and the remaining entities. #580 stays open.
