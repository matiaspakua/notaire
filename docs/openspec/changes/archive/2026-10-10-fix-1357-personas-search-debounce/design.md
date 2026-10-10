# Design

## Context

`GET /people/search` returns an unpaged list of matches for `firstName`, `lastName`, `identificationNumber` and `isClient`. The personas page drove it straight from three inputs and a client toggle.

## Goals / Non-Goals

Goal: one request per pause in typing, a stable primitive cache key, no flashing table. Non-goals: server-side unified search, other search pages.

## Decisions

Debounce the whole criteria object with one timer (not each input), so changing two fields quickly is still one request. Each settled criteria set is its own cache entry; TanStack Query renders only the current key's data, so an earlier response can never overwrite a later one. `keepPreviousData` keeps the last results visible while the next search loads. Clearing every criterion disables the query and the table shows the cached server page.

## Riesgos / Trade-offs

Results appear 300ms after the last keystroke instead of immediately; the issue asks for that delay.

## Testing Strategy

`personas-search.test.tsx` (5 failing first: hook missing) with fake timers: 300ms constant, primitive key, idle without criteria, 8 quick keystrokes give 1 request with the final text, previous results kept while loading. TS-0015 `#1357` (failing first: 17 requests).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0015, TS-0111 and the chromium suite against the branch build.

## Playwright Strategy

TS-0015 `#1357`: type a seeded surname key by key and assert exactly one `/people/search` request carrying the full surname, and the person in the table.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
