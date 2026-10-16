> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after testimony ports-only.

## Goals / Non-Goals

**Goals:** Clear R1 edges without changing REST contracts or CU11/CU12/CU44 behaviour.
**Non-Goals:** Domain model extraction; FE; CHANGELOG.

## Decisions

OpsPort returns DTOs; circuit methods keep throwing BusinessValidationException/ResourceNotFoundException to GlobalExceptionHandler.

## Riesgos / Trade-offs

SimpleControllersTest and PersonConstraintErrorsTest must mock inbound ports instead of repository.

## Testing Strategy

Update SimpleControllersTest + PersonConstraintErrorsTest; existing integration tests.

## Regression Strategy

TestimonyMovementControllerIntegrationTest and CatalogControllersIntegrationTest.

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
