> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The page calls the shared api-client helpers through the folio and folio-type hexagon adapters; the tests mock those helpers.

## Goals / Non-Goals

**Goals:** fast tests for the page's business rules.
**Non-Goals:** changing the page.

## Decisions

['**Floors = measurement minus about two points**, per the code-quality policy.', '**Guard test first**: the documented floors are raised before the config, so the guard is red until the config follows.']

## Riesgos / Trade-offs

['[a later PR drops coverage under the new floors] -> that PR adds tests; the floor is only lowered with an ADR.']

## Testing Strategy

The thresholds guard is red with the old config and green with the new one; the full coverage run (213 files, 1517 tests) passes the new thresholds.

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
