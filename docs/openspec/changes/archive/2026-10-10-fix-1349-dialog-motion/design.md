# Design

## Context

Tailwind v4 with shadcn components that assume tw-animate-css.

## Goals / Non-Goals

Goal: the intended dialog motion. Non-goals: a motion-token system (epic item).

## Decisions

Option A (install the plugin) keeps the standard shadcn classes and needs no component rewrite; explicit open/closed variants so the exit animation exists.

## Riesgos / Trade-offs

SearchCombobox (#1405) uses `animate-in fade-in slide-in-from-top-1`, which now also animates (150ms, motion-reduce:animate-none).

## Testing Strategy

TS-0117 failed first: the dialog's animation-name was none and nothing stayed mounted for an exit animation.

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0117 and the chromium suite against the branch build.

## Playwright Strategy

TS-0117: the dialog and overlay animate in; the closed dialog animates out before unmounting; under reduced motion the animation lasts under 50ms.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
