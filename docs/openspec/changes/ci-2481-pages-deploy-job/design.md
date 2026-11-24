> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Runs 45351169698 and 45353857070 failed with 'No artifacts named github-pages'; re-runs passed.

## Goals / Non-Goals

**Goals:** First-attempt deploys.
**Non-Goals:** Changing the build.

## Decisions

Use GitHub's two-job layout rather than a retry loop.

## Riesgos / Trade-offs

None beyond the deploy itself; permissions are workflow-level.

## Testing Strategy

Red first: two-job layout invariant; actionlint.

## Regression Strategy

Workflow invariants, concurrency, Pages metrics tests (65).

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
