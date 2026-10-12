# Design

## Context

`ProcedureTemplateResponse` has no required/optional flag: a row means the document is required. Names come from the procedure-type and document-type catalogs.

## Goals / Non-Goals

Goals: list every template, filter, jump to the editor, 390px. Non-goals: an optional/required flag (not in the model).

## Decisions

Added as a section on the existing templates page rather than a tab, matching the page's stacked sections. The 'Condición' column shows 'Requerido' for every row so the issue's required/optional column exists without inventing data.

## Riesgos / Trade-offs

One extra GET on the templates page; the list is small (procedure types × document types).

## Testing Strategy

Written first: Vitest red (hook and component missing); TS-0145.

## Regression Strategy

quick.sh; TS-0131/TS-0133/TS-0142 on chromium.

## Playwright Strategy

TS-0145 desktop + 390px.

## Deployment Strategy

Frontend only; rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
