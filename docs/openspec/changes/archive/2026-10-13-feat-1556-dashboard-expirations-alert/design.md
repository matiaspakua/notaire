# Design

## Context

`GET /submitted-documents/upcoming-expirations?days=N` already backs the CU42 page (default window 30).

## Goals / Non-Goals

Goals: count + 3 nearest + link, hidden when empty/error, mobile-friendly. Non-goals: a configurable window on the dashboard, notifications.

## Decisions

Reuse the page's hook and default so both show the same set; sort client-side by `daysRemaining`. An error hides the card rather than showing an error block, so the dashboard never breaks for an optional alert.

## Riesgos / Trade-offs

One extra GET on the dashboard; cached and shared with the page.

## Testing Strategy

Written first: Vitest red (component missing); TS-0020 CU42-GW01 replaces the skip.

## Regression Strategy

quick.sh (typechecks, Process Checks).

## Playwright Strategy

TS-0020 CU42-GW01 desktop + 390px, 6/6 over 3 repeats on chromium.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
