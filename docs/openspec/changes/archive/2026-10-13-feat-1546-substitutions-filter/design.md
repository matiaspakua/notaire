# Design

## Context

`GET /substitutions` returns the whole list (small table); `dateEnd` may be empty.

## Goals / Non-Goals

Goals: year and notary filters, URL-persisted, mobile-friendly. Non-goals: server-side filtering.

## Decisions

Client-side, as the issue specifies. Years are read from the ISO text so a midnight timestamp can't move to the previous year. A missing `dateEnd` counts as a one-year span from the start.

## Riesgos / Trade-offs

None; read-only.

## Testing Strategy

Written first: Vitest red (module missing).

## Regression Strategy

`frontend/verify.sh`; TS-0017 on chromium.

## Playwright Strategy

TS-0017 CU59-GW01/02/03 (12/12 over 4 repeats).

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
