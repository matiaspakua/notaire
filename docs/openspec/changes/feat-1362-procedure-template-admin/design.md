# Design

## Context

ProcedureTemplate has a composite key (procedure type, document type) and notes. DocumentType maps procedureTemplateList EAGER with CascadeType.ALL, and ProcedureType maps it LAZY with CascadeType.ALL.

## Goals / Non-Goals

Goals: list/add/edit/remove checklist rows from the procedure-type admin; correct names on the required-documents screen; a DELETE that deletes. Non-goals: ordering or a 'mandatory' flag (the API has only notes), changing the parent cascades (a wider entity change).

## Decisions

Bulk JPQL delete instead of removing the row from both parent collections: it is local to this repository and can't be undone by any cascade. Names are joined client-side from the small document-type catalog instead of widening the API response, so the contract stays unchanged. A dialog on the existing procedure-types page rather than a new route, as the issue suggested.

## Riesgos / Trade-offs

The client-side join shows '#id' for a document type missing from the catalog; acceptable, since the FK prevents it in practice.

## Testing Strategy

Written first: JUnit DELETE-then-404 (red: 200), Bruno 08/09 red on the main jar (11/13), Vitest dialog + join (red: module missing / name undefined), TS-0142 red on the pre-change build (2 failed).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, full Bruno, full chromium.

## Playwright Strategy

`TS-0142` admin adds, edits and removes a required document and the required-documents screen shows it; mobile 390px usability.

## Deployment Strategy

Backend + frontend; no migration. Rollback is reverting the PR.

## Rollback Strategy

Revert the commit.
