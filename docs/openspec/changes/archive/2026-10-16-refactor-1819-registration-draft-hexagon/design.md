> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after Notebook ports-only.

## Goals / Non-Goals

**Goals:** Clear R1 edge without changing REST contracts or CU82 behaviour.
**Non-Goals:** Domain model extraction; FE; CHANGELOG.

## Decisions

OpsPort returns DtoRegistrationDraft; adapter wraps existing RegistrationDraftService and maps exceptions to sealed results.

## Riesgos / Trade-offs

RegistrationDraftControllerTest must mock inbound ports instead of RegistrationDraftService.

## Testing Strategy

Update RegistrationDraftControllerTest; existing RegistrationDraftServiceTest and integration tests.

## Regression Strategy

RegistrationDraftControllerTest and RegistrationDraftReportIntegrationTest.

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
