# Design

## Context

`GET /api/v1/audit-log` pages when `page` or `size` is given and filters by `module` or `idUser` (mutually exclusive). Its page body uses `page` instead of Spring's `number`.

## Goals / Non-Goals

Goal: shared paging pieces, with the audit log as the first consumer. Non-goals: the other four lists (next slices of #1340), dashboard counters (#1358), row motion (#1368).

## Decisions

URL state through `router.replace` (no history spam, but reload and links keep the page). The module filter options come from a constant mirroring `AuditModuleResolver`, because a server-filtered list can't derive them from the loaded page. Text search has no backend parameter, so it filters the current page and says so.

## Riesgos / Trade-offs

A module the backend adds later isn't offered as a filter until it's added to `lib/audit-modules.ts`, though its rows still list and display. The follow-up is to expose the set through the API.

## Testing Strategy

`pagination.test.tsx` and `audit-log-pagination.test.tsx` (module missing, so red) and `TS-0104` (1000 rows instead of 20; no module select label) written first.

## Regression Strategy

`bash frontend/verify.sh`; Playwright chromium suite against the branch build.

## Playwright Strategy

`TS-0104`: 20 rows and the total, the oldest record on the last page, the page in the URL, no `size=1000` request, and the server-side module filter.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
