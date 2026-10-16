> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after Substitution (#1807).

## Goals / Non-Goals

**Goals:** ['ports-only ItemController', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG Unreleased', 'Deed/Person']

## Decisions

['OpsAdapter hydrates Budget via BudgetRepository; SaveResult carries BadRequest/NotFound/Conflict']

## Riesgos / Trade-offs

['ItemResponse is referenced from BudgetController — keep wire record public without entity from()']

## Testing Strategy

HexagonArchitectureTest; item integration/Bruno

## Regression Strategy

Bruno items; characterization tests

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
