> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers; the tests mock those helpers and render the real list view.

## Goals / Non-Goals

**Goals:** fast tests for the editor's actions.
**Non-Goals:** changing the page or testing React Flow itself.

## Decisions

1. **Drive the list view**, the keyboard path, instead of the canvas.
2. **Mock the api-client helpers**, as for #1670 to #1676.
3. **Prove the tests can fail** by breaking four rules once.

## Riesgos / Trade-offs

- [graph interactions untested] → Playwright covers drag and connect.

## Testing Strategy

The new file plus the full run (1097 tests); four mutation checks failed the intended tests.

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
