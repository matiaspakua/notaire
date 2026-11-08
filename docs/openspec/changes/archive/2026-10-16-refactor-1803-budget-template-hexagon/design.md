> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after ProcedureController (#1801).

## Goals / Non-Goals

**Goals:** ['ports-only BudgetTemplateController', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG Unreleased', 'BudgetController']

## Decisions

['OpsAdapter delegates to existing BudgetTemplateCatalogService so #574 exception mapping stays']

## Riesgos / Trade-offs

['BudgetTemplateProviderArchitectureTest must expect inbound ports']

## Testing Strategy

BudgetTemplateProviderArchitectureTest; HexagonArchitectureTest; BudgetTemplateControllerIntegrationTest

## Regression Strategy

Bruno budget-templates; characterization tests

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
