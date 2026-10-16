> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after RegistrationDraft ports-only.

## Goals / Non-Goals

**Goals:** Clear R1 edges without changing REST contracts or CU27/CU39 behaviour.
**Non-Goals:** Domain model extraction; FE; CHANGELOG; support Ref R1 edges.

## Decisions

OpsPort returns views; adapter wraps existing DocumentCostTemplateService; @Transactional moves off the controller.

## Riesgos / Trade-offs

DocumentCostTemplateControllerTest must mock inbound ports.

## Testing Strategy

Update DocumentCostTemplateControllerTest; existing DocumentCostTemplateServiceTest.

## Regression Strategy

DocumentCostTemplateControllerTest.

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
