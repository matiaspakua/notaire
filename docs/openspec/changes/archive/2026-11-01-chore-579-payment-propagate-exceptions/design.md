> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#579 has prior slices (safe ErrorResponses, constraint 400s). Payment still returns empty 500s.

## Goals / Non-Goals

**Goals:** Unexpected payment failures → GlobalExceptionHandler ErrorResponse.
**Non-Goals:** Rewrite all controllers; change deliberate empty-body 4xx.

## Decisions

1. Remove only `catch (Exception)`; keep IllegalArgument / PendingBalance catches.
2. Status remains 500; body gains ErrorResponse (clients that only checked status stay OK).

## Riesgos / Trade-offs

- [Risk] Bruno asserts empty body → Mitigation: payment 500 paths are rare; unit test covers shape.

## Testing Strategy

PaymentControllerTest red (empty body) → green (ErrorResponse).

## Regression Strategy

PaymentControllerTest full class; CI Bruno/Playwright.

## Playwright Strategy

n/a — error-path unit characterization; happy-path E2E unchanged.

## Deployment Strategy

Next merge.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
