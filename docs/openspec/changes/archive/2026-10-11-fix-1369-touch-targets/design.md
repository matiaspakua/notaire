# Design

## Context

13 list pages render row actions with `Button size="sm"`; editing each page would conflict with #1340, #1360 and #1361, which touch the same pages.

## Goals / Non-Goals

Goals: 44x44 on coarse pointers for every small button and the rows-per-page select; 8px between row actions; no desktop change. Non-goals: redesigning the row-action layout, changing the default button size.

## Decisions

Tailwind v4's built-in `pointer-coarse:` variant (`@media (pointer: coarse)`) instead of a viewport breakpoint: a 1024px tablet still gets large targets and a narrow desktop window keeps the compact look. `min-w-11` (not `w-11`) so text buttons keep their padding.

## Riesgos / Trade-offs

Touch rows get taller (44px actions); the mobile card layout (#1356) already has room. Laptops with touch screens report a fine primary pointer and keep 32px.

## Testing Strategy

Vitest `button.test.tsx`: `sm` and `icon-sm` classes, rendered icon-sm button, no `gap-1` container of small buttons in `src/`, select height on coarse pointers. Written first (red commit 47d9189f).

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite.

## Playwright Strategy

`TS-0122-touch-targets.spec.ts`: on a 390px phone and an 820px tablet with touch, every visible button and select in `main` on personas, gestiones, presupuestos and usuarios is at least 44x44; on a 1440px desktop the personas row action stays 32px tall. Observed failing on the pre-change build (8 touch cases).

## Deployment Strategy

Frontend only.

## Rollback Strategy

Revert the commit.
