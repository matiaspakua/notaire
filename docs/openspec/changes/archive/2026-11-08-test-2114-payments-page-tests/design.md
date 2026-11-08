> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers through the folio and folio-type hexagon adapters; the tests mock those helpers.

## Goals / Non-Goals

**Goals:** fast tests for the page's business rules.
**Non-Goals:** changing the page.

## Decisions

['**Mock the api-client helpers**, as for #1670 to #2111.', '**Replace the budget picker by a test double.**', '**Prove the tests can fail** by breaking four rules once.']

## Riesgos / Trade-offs

['[the picker double hides the combobox] -> the budget picker has its own suite.']

## Testing Strategy

The new file plus the existing payments suites (43 tests); four mutation checks failed the intended tests.

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
