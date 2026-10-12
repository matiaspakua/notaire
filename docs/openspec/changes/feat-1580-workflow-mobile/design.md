# Design

## Context

#1578 added the list view; this makes it the phone's editing surface.

## Goals / Non-Goals

Goals: no horizontal overflow at 390px, editing possible on a phone, graph still viewable. Non-goals: a collapsed overflow menu for header actions.

## Decisions

Wrap header actions rather than collapsing them into a menu (fewer taps, no new component). The AppHeader change applies to every page; the mobile specs (TS-0109, TS-0041, TS-0122 and others) were rerun and pass.

## Riesgos / Trade-offs

Branch is based on #1578's branch; it merges after #1585, #1586, #1587 and #1578's PR.

## Testing Strategy

Written first: Vitest red (helpers missing, header not wrapping); TS-0153.

## Regression Strategy

quick.sh; workflow suite and every spec mentioning 390/375/mobile on chromium (250 passed).

## Playwright Strategy

TS-0153.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
