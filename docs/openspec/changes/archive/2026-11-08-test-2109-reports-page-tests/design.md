> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers through the folio and folio-type hexagon adapters; the tests mock those helpers.

## Goals / Non-Goals

**Goals:** fast tests for the page's business rules.
**Non-Goals:** changing the page.

## Decisions

['**Mock the report hooks and the catalog use cases**, as for the existing reports page test.', '**Use a small select double** (Radix Select in jsdom focus scope).', '**Prove the tests can fail** by breaking four rules once.']

## Riesgos / Trade-offs

['[mocked hooks hide the PDF download] -> useReports-contract tests cover the request.']

## Testing Strategy

The new file plus the existing reports suites; four mutation checks failed the intended tests.

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
