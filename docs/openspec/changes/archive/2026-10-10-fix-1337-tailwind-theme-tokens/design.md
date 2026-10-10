# Design

## Context

Tailwind v4 (`@import "tailwindcss"`, no `tailwind.config`) reads colours from `@theme`. The project migrated from v3 without porting the shadcn colour mapping.

## Goals / Non-Goals

Goal: make the existing semantic classes produce CSS and meet AA on primary. Non-goals: token single source and dark-mode removal (#1365), table header contrast (#1341).

## Decisions

`@theme inline` with `hsl(var(--token))`: the `:root` variables stay the runtime source, and Tailwind emits `color-mix()` for `/90`-style modifiers (checked in the build output). `--radius` is not mapped, so `rounded-*` keeps today's look.

## Riesgos / Trade-offs

Pages now show the intended colours: primary buttons turn blue, muted text turns the documented gray (#666, 5.7:1), destructive red returns. That is the intended visual change.

## Testing Strategy

`theme-css-tokens.test.ts` (21 of 22 cases red before the change) and `TS-0101` (2 red against a `main` build) were written first.

## Regression Strategy

`bash frontend/verify.sh` (tsc, eslint, vitest, next build); Playwright TS-0001-TS-0101 chromium suite against the branch build.

## Playwright Strategy

`TS-0101` checks computed colours on `/login` and `/dashboard/personas`; screenshots before/after in the PR.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
