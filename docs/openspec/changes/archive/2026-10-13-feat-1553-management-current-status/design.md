# Design

## Context

`DtoHistorySummary` has no recorder field; `idHistory` is null when the summary is synthesized from the management's status.

## Goals / Non-Goals

Goals: status, date, notes; 404 state; refresh after transition; 390px. Non-goals: showing who recorded it (not in the DTO).

## Decisions

Placed in the existing case summary dialog (the management's detail view). The query key sits under `managementsKeys.all`, which `useTransitionManagement` already invalidates.

## Riesgos / Trade-offs

One extra GET when the dialog opens.

## Testing Strategy

Written first: Vitest red (component missing); TS-0144.

## Regression Strategy

quick.sh; TS-0028/TS-0029/TS-0098/TS-0109 on chromium.

## Playwright Strategy

TS-0144 desktop + 390px, 4/4 over 2 repeats.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
