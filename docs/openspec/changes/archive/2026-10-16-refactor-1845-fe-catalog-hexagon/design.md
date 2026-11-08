> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693. Final FE suppressions cluster.

## Goals / Non-Goals

**Goals:** Clear the last four FE eslint suppressions.
**Non-Goals:** Do not expand catalog UI.

## Decisions

Four small cores mirroring existing CRUD hooks; item gateway includes budget and discounts-surcharges list endpoints.

## Riesgos / Trade-offs

Item create invalidation depends on fkIdBudget.idBudget shape.

## Testing Strategy

Vitest use-cases + gateways + architecture-boundaries.

## Regression Strategy

Items by budget; discounts-surcharges; copies/substitutions/properties CRUD.

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
