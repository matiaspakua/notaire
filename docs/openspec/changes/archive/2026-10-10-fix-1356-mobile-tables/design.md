# Design

## Context

All list pages share `components/shared/DataTable.tsx`, so one component change covers them all. Many Playwright specs locate rows with `getByRole("row")` or a single `getByText`, and some run at 320-390px.

## Goals / Non-Goals

Goal: no horizontal scroll and visible actions on phones; desktop unchanged. Non-goals: per-page card tuning, swipe gestures, the `⋯` overflow menu.

## Decisions

The issue proposed rendering both layouts and toggling them with `md:hidden` / `hidden md:table`. That duplicates every cell and button in the DOM, which breaks strict-mode locators (`getByText`, `getByRole` with hidden elements) across the existing suite and doubles the work for long lists. The card list is chosen with a `matchMedia` hook instead; its server snapshot is `false` so hydration matches the server HTML, and the cards replace the table right after hydration. The card title defaults to the first non-`id` column because every page puts the identifying field (name, number, date) right after the ID.

## Riesgos / Trade-offs

On phones the table flashes for one frame before the cards appear; an `md:` CSS fallback is unnecessary because hydration happens before data arrives in practice (lists load client-side).

## Testing Strategy

`data-table-mobile.test.tsx` (6 of 7 failed: no card layout) and `TS-0109` (5 of 6 failed against the main build) written first.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

`TS-0109`: at 390x844 personas, gestiones, presupuestos, escrituras and pagos (stubbed one-row lists) render cards, the edit button is inside the viewport with a 44px target, and the page has no horizontal overflow; at 1440px the table stays.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
