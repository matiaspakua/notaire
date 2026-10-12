# Design

## Context

The proxy redirects any `/login` request that carries the UX status marker to `/dashboard`. The marker is cleared client-side by `logout()` right before `window.location.assign`, and under load the navigation can still carry it.

## Goals / Non-Goals

Goals: the expired message is always shown; TS-0093 is deterministic. Non-goals: changing the 401 contract or the cookie design.

## Decisions

`expired=1` is only ever produced by the expiry handler, so treating it as 'session is over' at the edge is safe. Clearing the markers there removes the stale state, whatever the client managed. The layout guard change removes a second client-side race.

## Riesgos / Trade-offs

A logged-in user who types `/login?expired=1` sees the login page and loses the UX markers. The next protected route redirects to login, and logging in again restores them. Harmless.

## Testing Strategy

Written first: Vitest proxy case and layout case red before the fix. Request trail captured with a temporary debug spec (not committed).

## Regression Strategy

`frontend/verify.sh`; TS-0093 at `--repeat-each=30 --workers=6`; full chromium suite via CI.

## Playwright Strategy

TS-0093 updated; no new scenario.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
