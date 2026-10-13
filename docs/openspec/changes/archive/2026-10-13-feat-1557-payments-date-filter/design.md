# Design

## Context

`GET /payments/date?startDate&endDate` already existed with `BETWEEN` on a DATE column. The budget consult (#1544) already keeps `?budget=` in the URL.

## Goals / Non-Goals

Goals: inclusive range, URL-persisted, mobile-friendly, works with the budget consult. Non-goals: paging the date result.

## Decisions

Server query when no budget is chosen (scales with the table); client-side narrowing when a budget is chosen (its list is already loaded and small). Days compare as `yyyy-MM-dd` from the shared `toDateInputValue`, so a server instant for local midnight stays on its day.

## Riesgos / Trade-offs

The 400 for an inverted range changes a degenerate request's status; no caller relied on it (no UI caller before).

## Testing Strategy

Written first: controller tests and Vitest red before the implementation.

## Regression Strategy

quick.sh (typechecks, Process Checks); `PaymentControllerTest`.

## Playwright Strategy

TS-0014 16 passed, 1 skipped (CU15-GW03) on chromium.

## Deployment Strategy

No migration; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
