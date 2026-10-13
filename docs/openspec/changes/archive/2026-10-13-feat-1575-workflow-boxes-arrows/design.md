# Design

## Context

#1574 made the boxes fixed-size for the layout; this gives them content and the edges direction.

## Goals / Non-Goals

Goals: readable type without color, visible direction, readable labels, selection feedback. Non-goals: editing panel (#1576), zoom toolbar (#1579), mobile (#1580).

## Decisions

Shape plus text badge for type (#646 color-only rule). Labels truncated on the canvas instead of growing columns without bound; the full description stays in the edge data for the editing panel (#1576). Mounting the canvas after load is the minimal fix for fitView running on an empty graph; the explicit fit toolbar is #1579.

## Riesgos / Trade-offs

Some seeded transition descriptions are English (data, not UI).

## Testing Strategy

Written first: Vitest red (component and edge factory missing); TS-0148.

## Regression Strategy

quick.sh; TS-0147, TS-0035, TS-0138, TS-0107 on chromium.

## Playwright Strategy

TS-0148 3 scenarios.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
