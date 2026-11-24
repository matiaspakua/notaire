> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#2449 added a release-please gate; it failed four times on the release PR but runs on main pushes and cannot block the PR.

## Goals / Non-Goals

**Goals:** Green main; the next release cannot break the same way.
**Non-Goals:** Blocking release PRs in GitHub settings.

## Decisions

Guard one train ahead instead of gating the bot PR. Pages CI runs `npm run prebuild` (the same generators the deploy uses).

## Riesgos / Trade-offs

Planning one train ahead needs a name early; v0.12 follows epic #2456.

## Testing Strategy

Red first: next-train guard, train_for, workflow order; Pages Vitest; product version coherence for v0.11.0.

## Regression Strategy

test_product_release, test_milestone_policy, Pages Vitest 122 tests.

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
