# Design

## Context

Edits are saved one by one as they happen, so 'saving' an invalid workflow means making it active (assignable to cases).

## Goals / Non-Goals

Goals: show which status is wrong, block activation of an invalid workflow, localized messages. Non-goals: blocking individual edits (a workflow is necessarily invalid while being built), backend error codes.

## Decisions

Gate on activation, not on each edit: building a workflow passes through invalid states. The backend validate decides; the client rules only localize and locate the problems. A new workflow cannot be created active because it has no statuses.

## Riesgos / Trade-offs

Branch is based on #1579's branch (PR #1586); it merges after #1585 and #1586. Saving an already active, invalid workflow (e.g. renaming) is also blocked until it is fixed or deactivated.

## Testing Strategy

Written first: Vitest red (module and mark missing); TS-0151.

## Regression Strategy

quick.sh; TS-0147..TS-0150, TS-0035, TS-0138, TS-0107 on chromium (TS-0021 fails locally on any build due to local data; CI covers it).

## Playwright Strategy

TS-0151.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
