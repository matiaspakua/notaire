> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers through the folio and folio-type hexagon adapters; the tests mock those helpers.

## Goals / Non-Goals

**Goals:** fast tests for the page's business rules.
**Non-Goals:** changing the page.

## Decisions

['**Extend the existing managements test files**, reusing their pickers and select doubles.', '**Prove the tests can fail** by breaking four rules once.']

## Riesgos / Trade-offs

['[picker doubles hide the comboboxes] -> the pickers have their own suites.']

## Testing Strategy

The two managements suites (43 tests); four mutation checks failed the intended tests.

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
