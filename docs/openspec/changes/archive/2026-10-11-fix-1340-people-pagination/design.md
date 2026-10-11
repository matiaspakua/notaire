# Design

## Context

`GET /api/v1/people` is a Spring `Page` (`@PageableDefault(size = 20, sort = "idPerson")`). `GET /people/search` returns an unpaged list of matches.

## Goals / Non-Goals

Goal: page the people list screen. Non-goals: person pickers in other forms, the dashboard counter (#1358), other lists (later slices).

## Decisions

Newest first (`idPerson,desc`), so a person just created appears on page 1 instead of after every older record. Search keeps the existing endpoint and shows all matches, because it already returns a bounded result set and has no paging. URL state through a shared `useUrlPagination` hook (`router.replace`).

## Riesgos / Trade-offs

A very broad search (for example the client filter alone) still returns every match in one response; paging `/people/search` is a backend follow-up.

## Testing Strategy

`personas-pagination.test.tsx` (failing first: hook missing, page not paged, URL ignored, no link outside the page; then the toaster pointer-events rule and the out-of-range page, each committed red before its fix) and `TS-0110` (2 failing first: 1000 rows instead of 20; toast link not usable).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0110, TS-0015 and the chromium suite against the branch build.

## Playwright Strategy

`TS-0110`: 20 rows newest first with the total, no `size=1000` request, the oldest person on the last page, the page kept on reload, and the duplicate toast opening an existing person outside the first page.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
