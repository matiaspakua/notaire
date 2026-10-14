> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The pages call the shared api-client helpers; the tests mock those helpers (and apiGetPage for the managements list) and route by path.

## Goals / Non-Goals

**Goals:** fast tests for six pages' business rules and the 80% line target.
**Non-Goals:** the other large pages and branch coverage.

## Decisions

1. **Mock the api-client helpers**, as for #1670 to #1673.
2. **Prove the tests can fail** by breaking each page's key rule once.
3. **Build no regex from strings** in the tests (CodeQL flagged one in #1672).

## Riesgos / Trade-offs

- [Radix select in jsdom] → the required documents test installs the pointer-capture and scrollIntoView polyfills in its own setup.

## Testing Strategy

The new files plus the full coverage run (1043 tests); the mutation checks failed the intended tests.

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
