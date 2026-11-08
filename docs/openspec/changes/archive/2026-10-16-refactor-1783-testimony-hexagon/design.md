> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Copy (#1779) and History (#1775) ports-only in flight. Testimony is the next mid-size mixed R1 hotspot (~5 edges).

## Goals / Non-Goals

**Goals:** ports-only TestimonyController; ArchUnit R1 shrink of five edges; REST unchanged; omit CHANGELOG (#1766).
**Non-Goals:** TestimonyMovementController; full domain extraction; CHANGELOG while #1766 open.

## Decisions

1. Prefer inbound Manage/Query ports returning DtoTestimony / sealed results. 2. Reuse TestimonyGenerationVerificationService inside TestimonyOpsAdapter. 3. Keep TestimonyCreateRequest/UpdateRequest on the web adapter (DTO subclasses). 4. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [ErrorResponses create/update failed paths] → SaveResult.Failed carries Exception for ErrorResponses mapping.
- [Constraint-error unit test] → update collaborator map to ManageTestimoniesUseCase.

## Testing Strategy

SimpleControllersTest$TestimonyControllerTests, TestimonyControllerIntegrationTest, TestimonyPersonConstraintErrorsTest, HexagonArchitectureTest.

## Regression Strategy

mvn test -pl backend-api -Dtest=SimpleControllersTest,TestimonyControllerIntegrationTest,TestimonyPersonConstraintErrorsTest,HexagonArchitectureTest

## Playwright Strategy

n/a — no UI surface (testimony REST contract unchanged).

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
