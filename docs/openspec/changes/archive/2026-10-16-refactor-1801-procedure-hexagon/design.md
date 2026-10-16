> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after ProcedureTemplate (#1798).

## Goals / Non-Goals

**Goals:** ['ports-only ProcedureController', 'clear R1 edges']
**Non-Goals:** ['CHANGELOG Unreleased', 'FE procedures']

## Decisions

['Mirror Copy/ProcedureTemplate OpsPort pattern; hydrate associations in adapter (#981)']

## Riesgos / Trade-offs

['Nested response refs must match prior JSON shape']

## Testing Strategy

SimpleControllersTest ProcedureControllerTests; HexagonArchitectureTest; ProcedureControllerFkHydrationIntegrationTest

## Regression Strategy

Bruno procedure API; existing integration hydration tests

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
