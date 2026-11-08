> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Release-please #1718 cleared CHANGELOG Unreleased; Features line lands with this PR.

## Goals / Non-Goals

**Goals:** FE spans in Tempo as notaire-frontend after page load + API proxy; Unreleased Features.
**Non-Goals:** Mac re-smoke ownership (#1740).

## Decisions

1. BFF proxy (not published collector port) so browser never sees Docker DNS.
2. Opt-in public flag; compose sets true for full stack.
3. CHANGELOG Unreleased Features after #1718 freed the lane.

## Risks / Trade-offs

Without collector, proxy returns 503 — exporters should not crash the UI.

## Testing Strategy

Failing vitest for proxy + init config first, then implement.

## Regression Strategy

Existing api-client and metrics routes stay green.

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
