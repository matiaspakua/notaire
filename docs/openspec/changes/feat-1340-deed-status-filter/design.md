# Design

## Context

Deed statuses are free text stored with the `BusinessConstants` spellings. `DeedRepository` already had `findByStatus(String)` (a list); the paged list ignored status.

## Goals / Non-Goals

Goals: one optional, documented status filter on the paged list. Non-goals: a deed status enum or migration, the deed pickers (slice 8b), changing `/escrituras/buscar`.

## Decisions

Canonicalise the value in the controller against the five known spellings (case-insensitive) and query by the stored spelling, so no case-insensitive SQL is needed and the index on status (if any) stays usable. Unknown values raise `BusinessValidationException` (400 with the accepted values), the same contract as the budget status filter (#1346).

## Riesgos / Trade-offs

Rows with a status outside the five spellings can only be listed without the filter; none are written by the application.

## Testing Strategy

`DeedStatusFilterIntegrationTest` (H2): filter returns only matching deeds with their own total; any letter case; no filter lists everything; unknown → 400 naming the values; OpenAPI documents the optional parameter with its enum. Written first: 4 of 5 failed (the no-filter regression case passed).

## Regression Strategy

`bash backend-api/verify.sh`; full Bruno collection on a backend built from the branch.

## Playwright Strategy

No UI change in this slice; slice 8b adds the e2e for the deed pickers.

## Deployment Strategy

Backend only, additive.

## Rollback Strategy

Revert the commit.
