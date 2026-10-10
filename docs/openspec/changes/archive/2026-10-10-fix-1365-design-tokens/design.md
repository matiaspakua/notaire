# Design

## Context

`:root` CSS variables are already the runtime source and are mapped by Tailwind v4 `@theme inline` (#1337). `tokens.ts` hex values are still needed by the React Flow canvas, SVG trackers and form patterns.

## Goals / Non-Goals

Goals: one runtime source, a guard against drift and raw palette classes, owner-decided brand and no dark mode. Non-goals: a dark theme, a categorical chart palette, removing the inline `style` usages in the SVG trackers (they already read tokens.ts).

## Decisions

- Keep `tokens.ts` as a typed hex mirror rather than `hsl(var(--x))` strings: contrast tests and SVG/canvas consumers need concrete colours. A Vitest sync check (±2 per channel) keeps the two equal.
- Status colours are text-safe (≥ 4.5:1 on white and on their own 10 % tint) so one token serves text, icons, borders (`/30`) and tints (`/10`).
- Module tiles: one neutral `bg-primary/10 text-primary` style (issue option 4a) instead of 14 hues.

## Riesgos / Trade-offs

Visual change on the dashboard tiles and status badges; no behaviour change.

## Testing Strategy

`design-tokens.test.ts` (13 failing) and TS-0119 (tile tint failing, 14 distinct styles) observed red first (test commits).

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0119 on /dashboard/personas and /dashboard (desktop + mobile screenshots).

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
