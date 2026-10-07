# Design

## Context

Issue #596 was rescoped to the tables that grow with use. `history` gains a row per state change and its list had no consumer in the frontend, so it can be paged without a UI change.

## Goals / Non-Goals

Goal: bound the history list. Non-goals: the other growing tables in #596, the per-management history endpoints (bounded by one management), and re-documenting `pageable` on the earlier paged endpoints.

## Decisions

Use `@ParameterObject` so the contract shows optional `page`/`size`/`sort` (adding optional parameters is not breaking); the only break, array→page, is inherent to pagination and is listed in `accepted-breaking-changes.txt` with its justification.

## Riesgos / Trade-offs

External clients that read `GET /api/v1/historial` as an array must read `content`; none ship in this repository.

## Testing Strategy

`HistoryPaginationIntegrationTest` (3 cases) written first and observed failing (the response was an array).

## Regression Strategy

Full backend suite; Bruno `history` folder; Playwright TS-0050 health matrix (68 passed, includes `GET /historial`) against this backend on PostgreSQL.

## Playwright Strategy

TS-0050 health project (68 passed).

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the commit.
