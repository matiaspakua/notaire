> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after audit #1890 merged (R2=30 on main @ f41c87243).

## Goals / Non-Goals

**Goals:** Clear ~8 registrationdraft R2 edges without behaviour change.
**Non-Goals:** History R2; FE changes; API contract changes.

## Decisions

Move entity-bound RegistrationDraftService beside OpsAdapter; delete unused entity RepositoryPort; keep existing OpsPort/DTO path used by Query/Write use cases.

## Riesgos / Trade-offs

Package move breaks tests that import the old service package — update imports.

## Testing Strategy

RegistrationDraftServiceTest + RegistrationDraftControllerTest + HexagonArchitectureTest shrink.

## Regression Strategy

Existing registration-draft Bruno/API tests and CU82 unit tests.

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
