# Design

## Context

#1495 translated identifiers across the codebase; a broad replacement also hit Spanish strings in es.json (27 values) and one Playwright assertion.

## Goals / Non-Goals

Goals: every es.json value Spanish again; a guard against regressions. Non-goals: reviewing English copy in en.json, renaming keys.

## Decisions

Values are restored from the last pre-rename commit rather than retyped, so wording matches what users saw. The guard uses a word list from the rename map instead of a dictionary, so it is precise and cheap; "Roles" is excluded because it is valid Spanish.

## Riesgos / Trade-offs

A future Spanish string that legitimately contains one of the listed English words (a brand or quote) would need the guard adjusted; none today.

## Testing Strategy

Vitest `es-catalog-spanish.test.ts` written first: 2 failures (27 leaked values) on main, green after the restore.

## Regression Strategy

`bash frontend/verify.sh`; Playwright TS-0023 and the full chromium suite in CI.

## Playwright Strategy

`TS-0023` users page shows the "Rol" column header in Spanish.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
