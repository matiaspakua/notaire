# Design

## Context

#1574 and #1575 made the graph readable; this makes it editable without hidden gestures.

## Goals / Non-Goals

Goals: every edit has a visible control, every delete is confirmed, invalid transitions get a clear message. Non-goals: canvas validation highlights (#1577), keyboard/list editing (#1578), mobile (#1580).

## Decisions

A side panel instead of popovers so the same surface can host the list view later (#1578). The duplicate check runs client-side for a clear message; the backend remains the source of truth. The transition PUT sends origin/destination unchanged because the endpoint requires them.

## Riesgos / Trade-offs

Branch is based on #1575's branch (PR #1584) because both change the same editor page; it merges after #1584.

## Testing Strategy

Written first: Vitest red (panel and guard missing); TS-0149.

## Regression Strategy

quick.sh; TS-0147, TS-0148, TS-0035, TS-0138, TS-0107 on chromium.

## Playwright Strategy

TS-0149 builds start → mid → final through the UI, edits a type, deletes a transition and a node.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
