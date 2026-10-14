> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

The pages call the shared api-client helpers; the tests mock those helpers (including apiGetPage for the managements list) and route by path.

## Goals / Non-Goals

**Goals:** fast tests for three pages' business rules.
**Non-Goals:** the other large pages and forms.

## Decisions

1. **Mock the api-client helpers**, as for #1670 to #1672.
2. **Prove the tests can fail** by breaking the confirmation flag, the hold-reason guard, the active-folder rule, the version resend and the in-use guards once.

## Riesgos / Trade-offs

- [Brittle selectors] → tests use the pages' existing `data-testid` values and translated button names.

## Testing Strategy

The new files plus the full coverage run (1007 tests); the mutation checks failed the intended tests.

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
