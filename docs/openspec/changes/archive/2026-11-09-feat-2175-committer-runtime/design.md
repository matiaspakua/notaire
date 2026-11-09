> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Owner-provided identity; closes configure-runtime task from factory-stop-after-2172

## Goals / Non-Goals

**Goals:** Evaluable advisory sole-committer on Notaire with Owner identity.
**Non-Goals:** required=true, Isolator implementation, other identities

## Decisions

Keep required=false; Owner may flip fail-closed in a follow-up Issue.

## Riesgos / Trade-offs

Agent tips may advisory-fail — expected and exit 0.

## Testing Strategy

WiringTest for evaluable + required false

## Regression Strategy

workspace.tests.test_aisdlc_adoption + kit committer tests

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
