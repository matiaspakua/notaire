# Design

## Context

The payment form already shows a budget's balance and status (#796/#821); consulting needed the same outside the form.

## Goals / Non-Goals

Goals: consult by budget, URL-persisted, mobile-friendly. Non-goals: date filtering (Q5, pending Owner decision; CU47-GW01 stays skipped).

## Decisions

Filter in place on the existing page instead of a new route, so CRUD actions keep working on the filtered rows. Balance from `/balance` (the bucket C endpoint), total from `/budgets/{id}/summary`.

## Riesgos / Trade-offs

None; read-only UI.

## Testing Strategy

Written first: Vitest red (hooks missing).

## Regression Strategy

`frontend/verify.sh`; TS-0014 on chromium.

## Playwright Strategy

TS-0014 CU47-BUDGET-01 (desktop: summary, 2 rows, reload, clear) and CU47-BUDGET-02 (390px, no overflow).

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
