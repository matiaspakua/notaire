> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after BudgetController (#1805).

## Goals / Non-Goals

**Goals:** ['ports-only SubstitutionController', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG Unreleased', 'Item/Deed/Person']

## Decisions

['OpsAdapter hydrates Person via PersonRepository; SaveResult.BadRequest when either person id is missing']

## Riesgos / Trade-offs

['SubstitutionControllerIntegrationTest must keep flat and nested person wire shapes']

## Testing Strategy

HexagonArchitectureTest; SubstitutionControllerIntegrationTest

## Regression Strategy

Bruno substitutions; characterization tests

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
