> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Workflow definition/node/transition hexagons done (#1728–#1730). Validation controller was left repo-only.

## Goals / Non-Goals

**Goals:** ports-only WorkflowValidationController; domain-typed validation; ArchUnit shrink; REST unchanged.
**Non-Goals:** Management mixed controller; deleting jpa/business workflow entities; CHANGELOG while #1766 open.

## Decisions

1. ValidateWorkflowUseCase returns sealed Result (NotFound | Ok). 2. Reuse Workflow*CatalogPort. 3. Move validation algorithm onto domain records; keep ValidationResult DTO shape for the wire. 4. Skip CHANGELOG (#1766).

## Riesgos / Trade-offs

- [Service tests use business entities] → rewrite fixtures to domain records.
- [Duplicate service/unit test] → update or remove obsolete duplicate.

## Testing Strategy

WorkflowValidationControllerTest, WorkflowValidationServiceTest, HexagonArchitectureTest shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=WorkflowValidation*,HexagonArchitectureTest

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
