> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

P0/P1 hex children #1725–#1732 filed slices done. #1758 was the Sonar handoff for remaining jpa S4973.

## Goals / Non-Goals

**Goals:** Clear S4973/RC pattern on version compares; inventory fix; English identifiers; pin test.
**Non-Goals:** Retiring jpa package; fixing all defer_hexagon smells; CHANGELOG while #1766 open.

## Decisions

1. Shared OptimisticLockVersions helper over per-file Objects.equals imports (many generated-style controllers). 2. Fix all controllers with the pattern, not only the inventory-listed subset. 3. Skip CHANGELOG Unreleased (#1766).

## Riesgos / Trade-offs

- [Behaviour change if == relied on identity] → versions are numeric; Objects.equals preserves intent.
- [Sonar live refresh unavailable] → inventory reclassified manually; seed default updated.

## Testing Strategy

JpaOptimisticLockEqualsTest; RemainingControllersJpaTest / ConceptJpaControllerTest smoke; inventory script unit tests.

## Regression Strategy

mvn test -pl backend-api -Dtest=JpaOptimisticLockEqualsTest,RemainingControllersJpaTest,ConceptJpaControllerTest

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
