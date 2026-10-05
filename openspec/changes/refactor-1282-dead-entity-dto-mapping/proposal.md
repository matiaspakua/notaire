# Delete the dead entity/DTO mapping methods (slice 2a of #577)

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1282 |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure |
| Branch | `refactor/1282_entity_dto_mapping_removal` |
| Gate 1 status | draft |

## Objetivo

The `business` entities carry Swing-era mapping methods (`getDto()`, `setAtributo(s)(dto)`, `toDto()`) that make the domain model depend on transport DTOs (#580) and put fields such as `dto` and `atributos` into the entities' JSON. After slice 1 (#1279) many of them have no production caller left. Delete exactly those, proven by the compiler, record what remains as a shrink-only baseline, and let a test keep it from growing back.

## What Changes

- 27 mapping methods with no production caller are deleted from 18 entities; the leftover `@JsonIgnore` of each deleted getter goes with it.
- Tests that only exercised a deleted method are deleted; assertions in shared coverage tests that verified values only a deleted method produced are removed with it. No other assertion is touched.
- `EntityDtoMappingRatchetTest` fails when an entity gains a mapping method that is not in `src/test/resources/architecture/entity-dto-mapping.txt`, and when the baseline lists a method that no longer exists, so the baseline can only shrink. It records the 27 live methods that later slices remove.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Domain entities do not map themselves to transport DTOs | #580, #577 | Made explicit |

## Capabilities

### New Capabilities

- `entity-dto-mapping-ratchet`: Entity mapping methods may only be removed, never added.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | yes | 18 entities lose 27 methods; one new test and baseline; entity unit tests trimmed |
| `frontend` | no | no endpoint changes |
| Docs / scripts / CI | yes | CHANGELOG |

### Surface area

- Endpoints, entities' columns, Flyway, configuration: none
- JSON: no OpenAPI schema changes (regenerated export is identical); the removed getters were not part of any documented response
- Dependencies: none
- Risk: a method reached only by reflection would compile but fail at runtime; mitigated by the full `mvn verify`, Bruno and Playwright suites, and by the unchanged OpenAPI export

### Architecture review

Continues #577 under `.claude/rules/refactoring.md`; no ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `CHANGELOG.md` | one entry |
