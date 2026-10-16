> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after folios admin.

## Goals / Non-Goals

**Goals:** Clear three eslint suppressions without changing REST or UI behaviour.
**Non-Goals:** Full concept catalog hexagon; CHANGELOG.

## Decisions

Separate cores for procedure-template, budget-template, document-cost-template; thin catalog lookups adapter for procedure-types and concepts until #1781 lands.

## Riesgos / Trade-offs

Merge conflict with #1781 if both touch concept adapters; keep concept list under templates-lookups path.

## Testing Strategy

Vitest: use cases + gateways + architecture pin; existing useProcedureTemplate test.

## Regression Strategy

Admin templates Playwright flows.

## Playwright Strategy

n/a — no UI behaviour change

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
