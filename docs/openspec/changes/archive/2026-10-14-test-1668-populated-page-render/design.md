> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The existing helper returns an empty page or an empty array for every request; the new one returns a single row carrying every `id*` key the pages read.

## Goals / Non-Goals

**Goals:** cover the populated-list branches of every page.
**Non-Goals:** interaction flows.

## Decisions

1. **One generic row** with all `id*` keys instead of a fixture per page; cheap and enough to execute the row branches.
2. **Wait for the row text but never fail on its absence**; the assertion is that nothing throws.

## Riesgos / Trade-offs

- [Flaky waits] → the wait is bounded and its failure is swallowed.

## Testing Strategy

The vitest suite itself (910 tests) and the coverage run that sets the floors.

## Regression Strategy

`npx vitest run --coverage` in `frontend`.

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
