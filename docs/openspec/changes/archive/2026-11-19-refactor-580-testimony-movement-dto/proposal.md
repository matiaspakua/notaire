# refactor: move testimony-movement transport mapping off the entity

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

| Field | Value |
|-------|-------|
| GitHub Issue | #580 |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure |
| Branch | `cursor/580-testimony-movement-mapper-47dc` |
| Gate 1 status | passed |

## Objetivo

`TestimonyMovement` built and applied `DtoTestimonyMovement`. The ops adapter and `Testimony` called those methods. This slice moves both off the entity.

## What Changes

- Add `TestimonyMovementLegacyMapper` with `toDto` and `apply`.
- The ops adapter and `Testimony` stop calling the entity methods.
- Delete `getDto` and `setAttributes`.
- Shrink the ratchet by both lines.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A movement DTO carries id, dates, registered, card, notes, observed, version, and the testimony id | existing methods | Made explicit |
| Applying a DTO keeps a null id and otherwise copies the same fields | existing `setAttributes` | Made explicit |

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

- `TestimonyMovementLegacyMapper.java`
- `TestimonyMovementOpsAdapter.java`
- `TestimonyMovement.java`
- `Testimony.java`
- `TestimonyMovementLegacyMapperTest.java`
- `EntitiesBasicTest.java`
- `entity-dto-mapping.txt`

### Architecture review

Follows ADR-030. `Testimony` copies the fields itself so `business` does not depend on the adapter. Located with `rg` because the symbol was known.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| Product docs | none |

## Out of Scope

- `Testimony.getDto` and `Testimony.setAttributes`. #580 stays open.
