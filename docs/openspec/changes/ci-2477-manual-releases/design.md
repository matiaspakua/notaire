> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The release-please gate (#2449) failed on the bot PR but could not block its merge; 0.9.0, 0.10.0 and 0.11.0 each broke main.

## Goals / Non-Goals

**Goals:** No release automation on main.
**Non-Goals:** Removing Release Please.

## Decisions

Keep Release Please for the changelog and version bumps, but only on dispatch. A `manual` cadence trigger requires a workflow whose only trigger is workflow_dispatch.

## Riesgos / Trade-offs

The Owner must dispatch twice per release; documented in RELEASE.md.

## Testing Strategy

Red first: trigger test; cadence test for the manual trigger.

## Regression Strategy

Release process tests (54), cadence tests, Pages Vitest.

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
