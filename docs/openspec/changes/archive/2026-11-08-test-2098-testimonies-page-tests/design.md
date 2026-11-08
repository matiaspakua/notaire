> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls use cases whose HTTP adapters use the shared api-client helpers; the tests mock those helpers.

## Goals / Non-Goals

**Goals:** fast tests for the page's business rules.
**Non-Goals:** changing the page.

## Decisions

1. **Mock the api-client helpers**, as for #2093.
2. **Use a small select double** (Radix Select in a Dialog recurses in the jsdom focus scope).
3. **Prove the tests can fail** by breaking four rules once each.

## Riesgos / Trade-offs

[select double hides Radix behaviour] -> Playwright covers the real dropdown.

## Testing Strategy

The new file plus the existing page suites; four mutation checks failed exactly the intended tests.

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
