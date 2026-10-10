# Design

## Context

`GET /presupuestos` is a Spring page sorted by idBudget desc; there is no text search on budgets, but budgets can be read by id and by person, and people have a server search.

## Goals / Non-Goals

Goal: no form lists budgets from the size=1000 list. Non-goals: a backend budget search endpoint, the deed and management pickers (next slice).

## Decisions

Search by client goes through `/people/search` and then `/presupuestos/persona/{id}` for at most 5 people, so a broad name costs at most 7 requests; a number is tried as a budget number and as a document number at once. The PersonPicker shell is generalised instead of copied, so both pickers keep one keyboard and announcement implementation.

## Riesgos / Trade-offs

A broad name lists only the budgets of its first 5 matching clients; typing more of the name or the budget number narrows it.

## Testing Strategy

`budget-picker.test.tsx` (failing first: module missing) and `TS-0114` (2 failing first: the budget select is a Radix button with no search).

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0114, TS-0014, TS-0011, TS-0111, TS-0042 and the chromium suite against the branch build.

## Playwright Strategy

`TS-0114`: the oldest budget (outside the newest 1000) is found by number in the payment form and its balance shows; a new client's budget is found by the client's name in the new-management form.

## Deployment Strategy

Frontend only; ships with the next frontend image.

## Rollback Strategy

Revert the commit.
