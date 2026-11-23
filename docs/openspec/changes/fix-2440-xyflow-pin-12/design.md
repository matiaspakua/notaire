> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

2e4fb7ac bumped 12.12.1 to 13.0.2; v13 onNodesChange receives a NodeChangeset.

## Goals / Non-Goals

**Goals:** main builds again; the next major cannot slip in silently.
**Non-Goals:** The v13 migration.

## Decisions

Pin instead of migrating now: the migration changes editor behaviour and needs its own Playwright coverage.

## Riesgos / Trade-offs

Low: returns to the version that shipped until today.

## Testing Strategy

Vitest guard (red on 13, green on 12); frontend build; Pages demo build via preflight.

## Regression Strategy

Existing workflow unit tests stay green.

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
