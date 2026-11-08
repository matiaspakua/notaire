> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

PropertyController builds its responses from the entity today; the new mapper reproduces the same fields. Procedure, report and registration-draft code keeps using the entity-returning PropertyRepositoryPort until its own slices.

## Goals / Non-Goals

**Goals:** replace a fake hexagon with a real one and delete dead code.
**Non-Goals:** changing validation or response shape.

## Decisions

1. **A new domain-typed port next to the old one** instead of changing the old port, because eleven other classes depend on it. 2. **Delete the unused inbound port and service** rather than adapt them; nothing calls them.

## Riesgos / Trade-offs

- [Two outbound ports for one aggregate during the transition] → the old one is documented as debt and shrinks as its callers migrate.
- [Create without an address] → pinned by a characterization test before the change.

## Testing Strategy

PropertyApiCharacterizationTest passes before and after; domain and use-case tests with an in-memory adapter; adapter test; existing property tests stay green.

## Regression Strategy

mvn verify -pl backend-api

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
