> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The populated fixture from #1668 supplies one row with every `id*` key; the templates page also reads a composite key.

## Goals / Non-Goals

**Goals:** cover dialog, form and confirmation branches.
**Non-Goals:** submitting forms; confirming deletions.

## Decisions

1. **Click once, never confirm.** Opening dialogs executes their render branches; confirming would call mutations and obscure failures.
2. **Skip disabled and detached buttons** so a dialog closing mid-loop does not fail the test.

## Riesgos / Trade-offs

- [A click handler throws asynchronously] → the run exits non-zero; verified with three repeats.

## Testing Strategy

The vitest suite (941 tests) and the coverage run that sets the floors.

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
