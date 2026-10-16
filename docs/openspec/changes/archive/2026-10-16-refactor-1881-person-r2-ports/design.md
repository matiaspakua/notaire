> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1880 notebook R2 (R2=46). Person is next (~8 edges).

## Goals / Non-Goals

**Goals:** Clear ~8 person R2 edges without behaviour change
**Non-Goals:** SubstitutionRepositoryPort / AuditRecordService / SubstitutionService Person edges

## Decisions

Move PersonService to adapter; delete dead PersonUseCase / PersonRepositoryPort; keep PersonOpsPort / PersonLookupPort

## Riesgos / Trade-offs

PersonOpsAdapter + unit/integration test import path updates only

## Testing Strategy

PersonServiceTest, PersonServiceIntegrationTest, PersonOpsAdapterDefaultTypeTest, HexagonArchitectureTest

## Regression Strategy

Person REST unchanged

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
