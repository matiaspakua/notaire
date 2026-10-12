# Design

## Context

#1576 added a side panel for the graph; the list view exposes the same actions without the canvas.

## Goals / Non-Goals

Goals: full definition with keyboard and screen reader, same rules and confirmations as the graph. Non-goals: phone layout (#1580).

## Decisions

Native tables, selects and buttons rather than a custom grid, for predictable keyboard and screen-reader behaviour. The repository has no axe dependency (TS-0107 is a custom contrast check), so accessibility is asserted with accessible-name checks in Vitest and Playwright instead of adding a new dependency.

## Riesgos / Trade-offs

Branch is based on #1577's branch (PR #1587); it merges after #1585, #1586 and #1587.

## Testing Strategy

Written first: Vitest red (component missing); TS-0152.

## Regression Strategy

quick.sh; TS-0147..TS-0151, TS-0035, TS-0138, TS-0107 on chromium.

## Playwright Strategy

TS-0152.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
