> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

`Folio.getDto` copied the notary, the folio type, and three deed fields. It did not call `Deed.getDto`, because that walks the deed's folio list and would recurse.

## Goals / Non-Goals

**Goals:**

- The folio entity imports no transport DTO.
- Callers still receive the same folio DTO fields.

**Non-Goals:**

- Removing `getDto` from `Testimony` or `Procedure`.

## Decisions

The mapper sits next to the other legacy mappers. `DeedLegacyMapper` may call it. `Testimony` keeps its own copy so `business` does not depend on the adapter.

## Riesgos / Trade-offs

- [The field list exists twice] → the mapper test covers it, including a missing deed.
- [A null folio type still throws] → that matches the previous method.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy, including notary, type, and deed | unit | `FolioLegacyMapperTest` |
| A missing deed stays null | unit | `FolioLegacyMapperTest` |
| The entity has no getDto | unit | `FolioLegacyMapperTest` |
| Existing folio assertions | unit | `EntitiesBasicTest`, `FolioTest` |

TDD: the mapper test did not compile until the class existed.

## Regression Strategy

- Mapper, folio, entities, deed mapper, and ratchet tests.
- `bash workspace/sdlc/preflight.sh --full`

## Playwright Strategy

n/a — no UI surface.

- Specs under `testing/e2e/tests/`: none
- `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway: no
- Smoke: the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none
