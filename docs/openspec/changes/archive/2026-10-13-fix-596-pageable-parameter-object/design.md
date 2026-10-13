# Design

## Context

Run 4 noted that springdoc rendered `Pageable` as a required object on the endpoints paged before #1322; the Owner approved switching them to `@ParameterObject`.

## Goals / Non-Goals

Goal: a coherent, valid contract for paging. Non-goals: paginating more endpoints (rest of #596).

## Decisions

`@ParameterObject` rather than hand-written `@Parameter`s, so defaults from `@PageableDefault` flow into the spec. The `pageable` removal is an oasdiff WARN, not ERR: the parameter never worked as documented (Spring ignores a `pageable` query key), so no deprecation period is needed and `accepted-breaking-changes.txt` stays untouched.

## Riesgos / Trade-offs

A generated client that sent `pageable` must send `page`/`size`/`sort`; that parameter was already ignored by the server.

## Testing Strategy

`PagedEndpointsOpenApiParametersIntegrationTest` written first and observed failing for the 5 endpoints.

## Regression Strategy

Full backend suite; Bruno people, budgets, deeds, procedures, managements against this backend on PostgreSQL 17.

## Playwright Strategy

Not applicable: documentation-only change; the frontend already sends page/size/sort.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the commit.
