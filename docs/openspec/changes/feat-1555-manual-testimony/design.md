# Design

## Context

`TestimonyCreateRequest` requires the deed; number, notes, flagged and verified are optional on the API but number is what identifies a testimony in the list.

## Goals / Non-Goals

Goals: deed, number, notes, verified; inline validation; 404/409 toasts; 390px. Non-goals: copies and movements in the create body (managed by their own flows).

## Decisions

Number is required in the UI (positive integer, same rule as the edit dialog). 'flagged' is sent false: flagging is the CU08 verify flow.

## Riesgos / Trade-offs

A manual number can duplicate a generated one; the backend has no uniqueness rule today.

## Testing Strategy

Written first: Vitest red (hook and dialog missing); TS-0146.

## Regression Strategy

quick.sh; TS-0031/TS-0143 on chromium.

## Playwright Strategy

TS-0146 golden, edge and 390px.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
