> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

Status derives the running stage from the workflow step's `stage` (aisdlc/status.py). `new_version` only replaces v1 id lists; approved workflows are immutable.

## Goals / Non-Goals

Goals: write v2 steps with stages from the CLI and the API, and map factory-feature onto the line. Non-goals: a UI editor.

## Decisions

- D1 Steps are validated on write (shape) and pinned on `define`/`request_validation` (refs), as today.
- D2 The API reuses the PUT path: write scope, CSRF and `If-Match` (`version_conflict` 409).
- D3 The approval of the new factory-feature version uses the Owner override (approvals_live=false), as the demo seed did, with the Owner's instruction recorded as the reason.

## Riesgos / Trade-offs

A wrong stage name would leave a step off the line; status ignores unknown stages, so nothing breaks.

## Testing Strategy

Unit tests for new_version with steps and each validation error; CLI edit; PUT with auth, CSRF, If-Match and conflict; status reports the step's stage.

## Regression Strategy

The full kit suite; existing workflow governance tests keep passing.

## Playwright Strategy

n/a — no UI change.

## Deployment Strategy

Ships with the kit.

## Rollback Strategy

Revert the merge; the previous factory-feature revision stays in the catalog history.

## Migration Plan

None.

## Open Questions

None.
