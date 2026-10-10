# Design

## Context

Tailwind v4 has an `--ease-*` theme namespace but no named-duration namespace, and tw-animate-css reads `--tw-duration` / `--tw-ease` for `animate-in` / `animate-out`.

## Goals / Non-Goals

Goals: one timing source, faster perceived navigation, consistent dialogs. Non-goals: skeletons (#1359), WorkflowTracker loops (#1353, merged separately), new presets beyond what the app uses.

## Decisions

- Named durations are `@utility duration-*` blocks that set both `transition-duration` and `--tw-duration`, so one class serves transitions and tw-animate-css animations.
- Route transition: drop `AnimatePresence mode="wait"` and the exit variant (the issue's "drop the exit" option) instead of `popLayout`, which would keep two pages mounted during the fade.
- Stagger cap via a `custom` index injected by `<Stagger>`, so existing call sites do not change.
- Stacked on #1365 because both edit globals.css, the dashboard page and the sidebar.

## Riesgos / Trade-offs

Visual timing only. Route-latency test uses a 300ms median budget (the old pipeline measured ~450ms on the agent box) so it is meaningful but not flaky on CI runners.

## Testing Strategy

`motion-tokens.test.ts` (module missing) and TS-0120 (median 451ms, dialog 0.2s) observed red first (test commit).

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

TS-0120 on /dashboard/copias ↔ /dashboard/items and /dashboard/personas (normal and reduced motion).

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
