> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ArchUnit R1 shrink; after Deed #1811.

## Goals / Non-Goals

**Goals:** Clear PersonController R1 edges; keep PersonResponse wire type for DeedController.
**Non-Goals:** Domain Person extraction; FE people admin hexagon.

## Decisions

Keep nested PersonResponse on controller without from(Person); map from PersonView. Conflict/Failed sealed results for duplicate and constraint failures.

## Riesgos / Trade-offs

Duplicate conflict body shape must remain {message, existingPersonId}.

## Testing Strategy

SimpleControllersTest Person nested tests; HexagonArchitectureTest.

## Regression Strategy

Bruno people CRUD; existing Person integration tests.

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
