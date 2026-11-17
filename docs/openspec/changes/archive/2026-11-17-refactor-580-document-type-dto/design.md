> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`getDto` and `setAttributes` on `business.DocumentType` are used by `EntitiesBasicTest` only. The catalog adapter maps fields itself.

## Goals / Non-Goals

**Goals:**

- The entity file imports no transport DTO.
- Due days stay null when the document does not expire.

**Non-Goals:**

- Changing the catalog port or the HTTP API.

## Decisions

The mapper sits next to `DocumentTypePersistenceAdapter`. Same field rules as the methods it replaces.

## Riesgos / Trade-offs

- [Other entities still map themselves] → #580 stays open.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy, due days cleared, no getDto on the entity | unit | `DocumentTypeLegacyMapperTest` |

TDD: the test did not compile until the mapper existed.

## Regression Strategy

- `EntitiesBasicTest` calls the mapper.
- Full suite: `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI surface.

- Specs to add/update under `testing/e2e/tests/`: none
- Command: `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway: no
- Configuration: none
- Smoke: the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none

## Migration Plan

One pull request. Further entities are later slices.

## Open Questions

None.
