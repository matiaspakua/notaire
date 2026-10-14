> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers; the tests mock those helpers, the two pickers and the select.

## Goals / Non-Goals

**Goals:** fast tests for the page's form and transition rules.
**Non-Goals:** changing the page.

## Decisions

1. **Mock the api-client helpers**, as for #1670 to #1675.
2. **Replace the select and pickers with test doubles** because Radix Select inside a Dialog recurses in the jsdom focus scope.
3. **Prove the tests can fail** by breaking five rules once.

## Riesgos / Trade-offs

- [test doubles hide Radix behaviour] → the select has its own tests and Playwright covers the real dropdown.

## Testing Strategy

The new file plus the managements suites (44 tests); five mutation checks failed the intended tests.

## Regression Strategy

`npx vitest run --coverage` in `frontend`; `python3 -m unittest discover -s testing/tests` for the inventory.

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
