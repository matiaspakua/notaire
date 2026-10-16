> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1865 budget R2 (R2=104). Document is next-highest fan-in (~19).

## Goals / Non-Goals

**Goals:** Clear ~19 document R2 edges without behaviour change
**Non-Goals:** Procedure RequiredDocumentationOnConfirmService (#1867); full domain.document aggregate rewrite

## Decisions

Move helpers to adapter (budget pattern); delete entity ports; Spring Data injected in adapters; inbound ports for CU10/CU42

## Riesgos / Trade-offs

ManagementController and UpcomingExpirationController wiring; DocumentCostTemplateOpsAdapter package import

## Testing Strategy

ExternalEntityDocumentServiceTest, UpcomingExpirationServiceTest, DocumentCostTemplateServiceTest, ManagementControllerDeleteLogTest, HexagonArchitectureTest

## Regression Strategy

Document/management document REST unchanged

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
