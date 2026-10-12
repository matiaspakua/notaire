# Design

## Context

Seed V10/V41 inserts nodes without positions; the e2e helper and the API create nodes at 0,0.

## Goals / Non-Goals

Goals: no overlap on load, persistent positions, free placement for new nodes. Non-goals: box styling, arrows, editing panel, validation, mobile (epic #1573 siblings).

## Decisions

dagre over elkjs: small, synchronous, enough for tens of nodes. Overlapping saved positions are treated as missing so legacy 0,0 rows get laid out without a data migration. Auto-layout is explicit (button) so the user's manual arrangement is never overwritten silently.

## Riesgos / Trade-offs

A very long chain fits small on screen; zoom/fit polish is #1579 and the mobile fallback is #1580.

## Testing Strategy

Written first: Vitest red (helper missing, editor blank); TS-0147.

## Regression Strategy

quick.sh; TS-0035/TS-0138 (viewer) on chromium.

## Playwright Strategy

TS-0147 2 scenarios.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
