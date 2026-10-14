> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The legacy business.IdentificationType entity is referenced by Person, Procedure and DeedManagement, so it stays as the persistence class for now; the adapter maps it to the pure domain type.

## Goals / Non-Goals

**Goals:** the first fully hexagonal catalog, used as the template for the next slices.
**Non-Goals:** touching other contexts.

## Decisions

1. **Factories separate creation from restoration.** Reading legacy rows must not fail validation; creating and renaming validate. 2. **Domain exceptions without status**, mapped by an advice that delegates to the global handler so response bodies stay identical. 3. **Delete flushes inside the adapter** so a foreign key violation becomes the in-use exception instead of surfacing at commit.

## Riesgos / Trade-offs

- [Delete of a referenced type used to answer 409 for any exception] → only data-integrity failures map to in-use now; other failures answer 500, which is more accurate and is documented in the PR.
- [Advice ordering] → the advice has highest precedence so the generic handler does not swallow domain exceptions.

## Testing Strategy

IdentificationTypeApiCharacterizationTest passes before and after; use-case unit tests with an in-memory adapter; adapter unit test for the integrity failure; existing integration tests stay green.

## Regression Strategy

mvn verify -pl backend-api; mvn test -pl backend-api -Dtest=HexagonArchitectureTest

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
