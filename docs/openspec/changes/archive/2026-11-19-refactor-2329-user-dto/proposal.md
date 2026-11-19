# refactor: move user transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2329 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/2329-user-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`User.setAttributes` copied a `DtoUser` and its person. Nothing in production called it. This slice moves that copy off the entity and reuses the person mapper.

## What Changes

- Add `UserLegacyMapper.apply`, which applies the person through `PersonLegacyMapper`.
- Delete `User.setAttributes` and the transport imports on the entity.
- Drop that ratchet line.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The linked person is copied with the same client rule as `PersonLegacyMapper` | existing method | Made explicit |
| A null user version leaves the current version | existing method | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper and User |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients. The catalog adapter does not call `setAttributes`.

### Write set

- `UserLegacyMapper.java`
- `User.java`
- `UserLegacyMapperTest.java`
- `entity-dto-mapping.txt`
- factory KPI notes for this sampled locate

### Architecture review

Follows ADR-030. This is the sampled locate. The Notaire incremental-refresh time after moving HEAD is recorded in `factory-kpis.json`.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |
| factory-kpis.json | the refresh measurement |

## Out of Scope

- `DeedManagement#getDto` and `Procedure#getDto`. #2329 stays open.
