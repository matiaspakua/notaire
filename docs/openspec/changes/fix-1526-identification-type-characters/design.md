# Design

## Context

`characters` is a String column, NOT NULL, holding values 8 to 12 in the seed and the dev data.

## Goals / Non-Goals

Goals: 400 instead of 500 for a missing value; reject non-numeric and out-of-range values; same rule in the admin form. Non-goals: switching the field to an integer (a type change for every client).

## Decisions

Keep the string type and add a pattern, the option the issue proposed without a type change. 1 to 50 covers every real document (passports and tax ids are under 20) and matches the issue's example. No leading zeros, so the stored value is canonical.

## Riesgos / Trade-offs

An external client that omits `characters` now gets 400 instead of 500, and one sending text gets 400 instead of 201. Both were already wrong; the break is listed for Owner review.

## Testing Strategy

Written first: JUnit (10 of 14 red: 201 instead of 400), Bruno 12/13 red on the main jar (0/2), Vitest red (51 accepted).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, full Bruno; TS-0139 (identification-type admin) on chromium.

## Playwright Strategy

No new scenario: the UI already blocks the invalid values; TS-0139 covers the admin page.

## Deployment Strategy

Backend + frontend; no migration (existing rows are 8 to 12). Rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
