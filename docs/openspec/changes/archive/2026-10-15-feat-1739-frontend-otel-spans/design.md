> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Hex fleet running (#1727+); omit CHANGELOG Unreleased this PR.

## Goals / Non-Goals

**Goals:** FE spans in Tempo as notaire-frontend after page load + API proxy.
**Non-Goals:** CHANGELOG Features line; Mac re-smoke ownership.

## Decisions

1. BFF proxy (not published collector port) so browser never sees Docker DNS.
2. Opt-in public flag; compose sets true for full stack.
3. Omit CHANGELOG while hex implementer is active.

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
