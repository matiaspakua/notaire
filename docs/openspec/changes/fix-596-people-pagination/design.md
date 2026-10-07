# Design

## Context

26 of 31 controllers return unbounded `findAll()` lists; people is one of the largest tables.

## Goals / Non-Goals

Goal: page the people list end to end. Non-goal: the other 25 list endpoints, and real UI paging (the screen still loads up to 1000 rows like the other paged lists).

## Decisions

Mirror the existing paged endpoints and `apiGetPaged`; keep the OpenAPI `pageable` parameter shape consistent with them.

## Riesgos / Trade-offs

Breaking for external API clients that expect an array; oasdiff reports 2 ERR changes (new `pageable` query parameter, response type array→object), the same as earlier pagination slices.

## Testing Strategy

`PeoplePaginationIntegrationTest` (2 cases) and `use-personas-pagination.test.tsx`, written first and observed failing.

## Regression Strategy

Backend full suite; Vitest; Bruno people folder; Playwright TS-0015/TS-0044/TS-0051 and full chromium project.

## Playwright Strategy

TS-0015 (personas workflow), TS-0051 (API full cycle, updated) and TS-0044 run green locally; full chromium project run before push.

## Deployment Strategy

Backend and frontend ship together (the frontend change is required by the new response shape).

## Rollback Strategy

Revert the commit (backend and frontend together).
