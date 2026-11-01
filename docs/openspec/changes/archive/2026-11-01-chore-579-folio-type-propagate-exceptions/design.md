> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#579 payment slice lands separately. FolioType still uses ErrorResponses catch-alls that turn
unexpected failures into 409.

## Goals / Non-Goals

**Goals:** Unexpected folio-type create/update failures → GlobalExceptionHandler ErrorResponse (500).
**Non-Goals:** Rewrite FolioTypeExceptionHandler; other controllers.

## Decisions

1. Remove `catch (Exception)` on create/update; keep `catch (FolioTypeException) { throw }` only if
   the compiler still needs it — prefer bare call so domain exceptions propagate naturally.
2. DataIntegrityViolationException and OptimisticLockingFailureException already have GEH handlers
   with the same safe messages as ErrorResponses.

## Riesgos / Trade-offs

- [Risk] Clients that treated create failure 409 as “any error” → Mitigation: only unexpected
  RuntimeException changes status; constraint paths stay 400/409 via GEH.

## Testing Strategy

FolioTypeControllerTest red (409 today) → green (500 ErrorResponse).

## Regression Strategy

FolioTypeControllerTest full class; CI Bruno/Playwright.

## Playwright Strategy

n/a — error-path unit characterization; happy-path E2E unchanged.

## Deployment Strategy

Next merge after #1990 (serialize heavy CI).

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
