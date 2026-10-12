# Design

## Context

Audit rows carry a timestamp; other date filters use `ZoneId.systemDefault()`.

## Goals / Non-Goals

Goals: server-side user and range filter, URL-persisted. Non-goals: free-text server search.

## Decisions

Specification instead of a JPQL with nullable params (H2/Postgres typing of null timestamps). Day bounds use the JVM zone, as `SubmittedDocumentController` and `UpcomingExpirationService` do.

## Riesgos / Trade-offs

Clients that sent module and idUser together now get both applied, not just module; that was the documented intent. Days follow the server zone, while the UI shows instants in the business zone. Same convention as other date filters.

## Testing Strategy

Written first: JUnit 5/5 red, Vitest 4/4 red.

## Regression Strategy

`backend-api/verify.sh`, `frontend/verify.sh`, Bruno in CI; TS-0016 and TS-0104 on chromium.

## Playwright Strategy

TS-0016 CU23-GW01: filter by admin and today, every row is admin's, inverted range shows the error (5/5 repeats).

## Deployment Strategy

Backend + frontend; no migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
