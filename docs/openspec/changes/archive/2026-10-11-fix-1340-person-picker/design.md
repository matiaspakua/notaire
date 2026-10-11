# Design

## Context

`GET /people/search` takes firstName, lastName, identificationNumber, idIdentificationType and isClient; first and last name together are ANDed, a document number wins, and isClient adds every client (a union), so it cannot be used as a filter.

## Goals / Non-Goals

Goal: no form lists people from the size=1000 list. Non-goals: paging `/people/search` (backend follow-up), the budget/management/deed lists (later slices).

## Decisions

Own combobox instead of a new dependency (no popover/cmdk in the project): an inline absolutely-positioned popup works inside Radix dialogs without portals. The client-only filter is applied on the client because isClient is a union on the backend. Escape is intercepted on window in the capture phase, because Radix dismisses dialogs from a document capture listener.

## Riesgos / Trade-offs

A one-letter search can return many matches; the popup shows the first 50. `/people/search` has no paging yet.

## Testing Strategy

`person-picker.test.tsx` (failing first: module missing; then focus-opening and stale options, each committed red before its fix) and `TS-0111` (2 failing first: no combobox).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0111, TS-0071, TS-0090, TS-0092, presupuesto-plantilla, TS-0011, TS-0010 and the chromium suite against the branch build.

## Playwright Strategy

`TS-0111`: a person created after the first 1000 is found by name in the budget form, picked by keyboard and saved; the management client filter finds a new client and filters by it.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
