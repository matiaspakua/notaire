> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

BudgetTemplate has a composite @EmbeddedId and implements Persistable<BudgetTemplatePK>; the repository was declared with Integer and is only used through derived finders.

## Goals / Non-Goals

**Goals:** same API, no static state, thin controller.
**Non-Goals:** changing response shapes of successes.

## Decisions

1. **Characterize first.** Tests of the current API are written and passed before the refactor and must stay green. 2. **Keep handler signatures** so the OpenAPI export and the loose-return ratchet do not change. 3. **Reuse the global handler** for 404/400/409 instead of per-method catches.

## Riesgos / Trade-offs

- [Error body shape changes from {error} to ErrorResponse] → no client reads the old body; the frontend parses ErrorResponse.
- [Optimistic locking on update] → the entity keeps @Version; the global handler maps the failure to 409.

## Testing Strategy

BudgetTemplateApiCharacterizationTest (MockMvc, H2) and BudgetTemplateProviderArchitectureTest.

## Regression Strategy

mvn verify -pl backend-api; mvn verify -pl backend-api -Ppg-integration

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
