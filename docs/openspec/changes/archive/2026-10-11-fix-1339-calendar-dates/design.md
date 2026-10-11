# Design

## Context

`java.util.Date` + `@Temporal(DATE)` is serialized by Jackson as an instant at the JVM's local midnight (Europe/Madrid on the dev box, UTC in containers). The browser zone is the user's (Argentina).

## Goals / Non-Goals

Goal: the same calendar day everywhere, for display, edit and defaults. Non-goals: locale-aware formats (#1355), backend `LocalDate` (backend follow-up).

## Decisions

An instant is read as the day of its nearest UTC midnight (instant + 12h, UTC date). That is the day the server meant for any server zone from UTC-12 to UTC+12, so it doesn't depend on knowing the server zone. `yyyy-MM-dd` and zone-less local midnights are taken literally. The business zone decides "today" and how real timestamps are shown. An alternative, interpreting instants in the business zone, would be wrong on the Madrid dev box and on UTC containers.

## Riesgos / Trade-offs

A real timestamp passed to `formatDate` would be rounded to the nearest midnight; the audit log now uses `formatInstant` explicitly. Management history is a TIMESTAMP column, but the backend stores the day's midnight in it (checked on the dev DB), so it stays a calendar date.

## Testing Strategy

`dates.test.ts` (5/3/4 failures in Buenos Aires/Madrid/UTC: formatDate and the source guard; module missing first), `TS-0102` (4 red) and `TS-0103` (2 red in Buenos Aires and UTC) written first.

## Regression Strategy

`bash frontend/verify.sh`; vitest under `TZ=America/Argentina/Buenos_Aires`, `Europe/Madrid`, `UTC`; Playwright chromium suite against the branch build.

## Playwright Strategy

`TS-0102` and `TS-0103` use `test.use({ timezoneId })` for Buenos Aires, Madrid and UTC.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
