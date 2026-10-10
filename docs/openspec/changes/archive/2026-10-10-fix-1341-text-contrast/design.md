# Design

## Context

#1337 made the semantic classes render, which exposed the light grays and reds that had never been painted before. The root cause is the token values, not the pages.

## Goals / Non-Goals

Goal: zero axe `color-contrast` nodes on all /dashboard/** routes and /login at desktop and 390px. Non-goals: brand primary redesign (owner kept #0071E3), dark mode (owner: remove for now, #1365).

## Decisions

Fix the tokens at the source so the ~40 inline `neutral[600]` uses improve without touching each page. The brand fill stays #0071E3 (white on it is 4.7:1); only blue *text* on gray uses the darker `--primary-text`, so buttons keep the brand colour. `--destructive` is darkened because it serves both as icon text on white and as a fill under white text.

## Riesgos / Trade-offs

Slightly darker grays and reds change the look; screenshots are attached to the PR. The TS-0107 checker skips text over gradients and images (as axe marks them for review) and only checks the visible viewport.

## Testing Strategy

`token-contrast.test.ts` (28 red, then 2 more for error text) and `TS-0107` (9 of 12 red on the #1376 build) written first.

## Regression Strategy

`bash frontend/verify.sh`; full Playwright chromium suite against the branch build; axe `color-contrast` on 36 routes at 1440px and 390px.

## Playwright Strategy

`TS-0107`: /login, /dashboard, personas, gestiones, reportes, administracion/workflows at 1440px and 390px.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
