> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

`Folio.getDto` called `fkIdFolioType.getDto()`. `setAttributes` on `FolioType` is only used by `EntitiesBasicTest`. `DtoFolioType.isValid()` always returns true, and `apply` keeps that check.

## Goals / Non-Goals

**Goals:**

- One legacy entity with no `com.licensis.notaire.dto` import.
- The same field copy as before.

**Non-Goals:**

- Rewriting `Folio.getDto` off DTOs.
- Touching other entities.

## Decisions

The mapper sits in the folio persistence adapter, next to `HistoryMapper`. `Folio` stays in `business` and copies the six folio-type fields itself so the domain package does not depend on the adapter.

## Riesgos / Trade-offs

- [The six setters drift from the mapper] → both are covered: the mapper test and the existing folio DTO tests that walk `Folio.getDto`.
- [Other entities remain coupled] → #580 stays open. This is the first leaf.

## Testing Strategy

| Scenario (spec) | Test level | Test class / file |
|-----------------|------------|-------------------|
| Fields copy onto the DTO and back | unit | `FolioTypeLegacyMapperTest` |
| The entity has no getDto or setAttributes | unit | `FolioTypeLegacyMapperTest` |

- New unit tests: `FolioTypeLegacyMapperTest`.
- New integration tests: none. No HTTP change.
- Coverage impact (JaCoCo raise-only floor; Owner >90% target #2046): the new mapper is exercised by the unit test.

TDD: the test did not compile until the mapper existed. The method-absence assertion fails if `getDto` returns.

## Regression Strategy

- Existing tests affected: `EntitiesBasicTest` now calls the mapper.
- Full suite command: `mvn -pl backend-api -Dtest=FolioTypeLegacyMapperTest,EntitiesBasicTest test`
- HTTP/Bruno API suite: unchanged
- Legacy paths at risk: `business.Folio.getDto` nested folio type

## Playwright Strategy

n/a — no UI surface.

- Specs to add/update under `testing/e2e/tests/`: none
- Golden path covered: n/a
- Edge / error paths covered: n/a
- Viewports: n/a
- Command: not added; `preflight.sh --full` still runs the suite

## Deployment Strategy

- Flyway migration required: no
- Deployment order / coupling: none
- Configuration or `.env` keys to add: none
- Feature flag: no
- Smoke test after deploy (Gate 5): the mapper unit test

## Rollback Strategy

- Revert safe: yes
- Database rollback: none needed
- Data written under the new behavior after revert: none
- Blast radius if rollback is delayed: mapping location only

## Migration Plan

One pull request. Further entities are later slices.

## Open Questions

None.
