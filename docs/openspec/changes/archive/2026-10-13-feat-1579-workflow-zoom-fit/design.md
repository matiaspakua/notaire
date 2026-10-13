# Design

## Context

#1574–#1576 fixed layout, boxes and editing; navigation of a long graph still needed work.

## Goals / Non-Goals

Goals: bounded zoom, labelled controls, fit shows all boxes on desktop and phone, a useful MiniMap. Non-goals: mobile list editing (#1580), keyboard editing (#1578).

## Decisions

React Flow's built-in Controls with `ariaLabelConfig` rather than a custom toolbar (already visible, keyboard focusable). Minimum zoom 0.1 (not 0.2) because a 9-column workflow is ~3000px wide; on a phone the graph is read-only and editing moves to the list view (#1580).

## Riesgos / Trade-offs

Branch is based on #1576's branch (PR #1585), which is based on #1575's (PR #1584); it merges after both.

## Testing Strategy

Written first: Vitest red (module missing; then node size missing); TS-0150.

## Regression Strategy

quick.sh; TS-0147, TS-0148, TS-0149, TS-0035, TS-0138, TS-0107 on chromium.

## Playwright Strategy

TS-0150 4 scenarios.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
