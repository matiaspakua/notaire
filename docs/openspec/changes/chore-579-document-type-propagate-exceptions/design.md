> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#579 folio-type / payment slices land separately. DocumentType create still uses
ErrorResponses catch-all → 409 for unexpected failures.

## Goals / Non-Goals

**Goals:** Unexpected document-type create failures → GlobalExceptionHandler ErrorResponse (500).
**Non-Goals:** Other controllers.

## Decisions

1. Remove create catch-all only; update/delete already propagate.
2. DataIntegrityViolationException already handled by GEH with safe messages.

## Riesgos / Trade-offs

- [Risk] Clients treated create failure 409 as “any error” → Mitigation: only unexpected
  RuntimeException changes status.

## Testing Strategy

DocumentTypeControllerTest red (409) → green (500 ErrorResponse).

## Regression Strategy

DocumentTypeControllerTest; CI Bruno/Playwright.

## Playwright Strategy

n/a — error-path unit characterization.

## Deployment Strategy

After #1992 merges (serialize heavy CI).

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
