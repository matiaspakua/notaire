# refactor: move testimony transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #2326 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-testimony-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`Testimony` built and applied `DtoTestimony`, including its deed and movements. The ops adapter called those methods. This slice moves both off the entity.

## What Changes

- Add `TestimonyLegacyMapper` with `toDto` and `apply`.
- The ops adapter stops calling the entity methods.
- Delete `getDto` and `setAttributes`.
- Shrink the ratchet by both lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A testimony DTO carries id, number, notes, flagged, verified, version, the deed, and the movements | existing `getDto` | Made explicit |
| Applying a DTO keeps a null id and copies a deed id and movements only when they are present | existing `setAttributes` | Made explicit |

## Capabilities

### New Capabilities

- None. The HTTP contract does not change.

### Modified Capabilities

- None. `skip_specs: true`.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | Mapper, adapter, Testimony |
| `frontend` | no | |
| `frontend-swing` | no | |
| `infra` | no | |
| CI/CD | no | |

### Surface area

- Endpoints: none
- Not **BREAKING** for API clients.

### Write set

- `TestimonyLegacyMapper.java`
- `TestimonyOpsAdapter.java`
- `Testimony.java`
- `TestimonyLegacyMapperTest.java`
- `TestimonyEntityTest.java`
- `EntitiesBasicTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. The mapper calls `Procedure.getDto` for nested procedures, the same call the entity made. This is the sampled A/B issue: `rg` 0.004s found the call sites; `repolens search Testimony.getDto` returned no results in 0.680s because the index has no such node.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |

## Out of Scope

- `Procedure.getDto`, `DeedManagement.getDto`, `Person.setAttributes`, and `User.setAttributes`. #2326 stays open. #580 closed when #2324 merged and could not be reopened.
