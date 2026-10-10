# Design

## Context

`PersonController.createPerson` resolved the type with `findById(id).orElse(defaultType)` and the default with `findById(1)`, so a typo stored DNI and the default depended on the id order of the catalog. About 40 backend tests, the Bruno collection and the e2e helpers create persons without a type.

## Goals / Non-Goals

Goals: the form always sends a type (DNI preselected), an unknown type id is a 400, the list shows the type. Non-goals: the admin catalog page (next slice), `characters`-based input limits, backfilling legacy persons.

## Decisions

- "Required" is enforced in the UI; the API keeps "omitted means DNI" on create. Rejecting an omitted type in the API would break every existing API client and test for no data gain, since the Owner's default is DNI anyway. What the API now refuses is the case that silently stored the wrong data: an id outside the catalog.
- The default type is looked up by name `DNI`, not by id 1.
- The type is resolved before the save `try`, so the `BusinessValidationException` reaches `GlobalExceptionHandler` (400 `ErrorResponse`) instead of the generic create-failed mapping.
- In the UI, the effective type is `editing.identificationTypeId ?? (create ? DNI : none)`, so DNI is preselected even when the catalog loads after the dialog opens, and an untyped legacy person shows an empty select and a field error on save.

## Riesgos / Trade-offs

A client that relied on the silent fallback now gets 400; none exists in the repo (Bruno sends id 1, e2e helpers send none). The list needs the catalog to print the type name; without it, the number alone is shown as before.

## Testing Strategy

JUnit `PersonIdentificationTypeIntegrationTest` (explicit CUIT stored, omitted → DNI, unknown id 400 on create, nested form 400, unknown id 400 on update and valid change 200); Vitest `personas-identification-type.test.tsx` (list label, DNI preselected and sent, untyped person blocked on save). Both written first (red commits).

## Regression Strategy

`bash backend-api/verify.sh`, `bash frontend/verify.sh`, full Bruno collection on the branch jar, full chromium suite.

## Playwright Strategy

TS-0015 `#1361`: a new person starts on DNI with the type select before the number; creating a person with CUIT persists it and the search result lists `CUIT <number>`. Observed failing on the pre-change build.

## Deployment Strategy

Backend and frontend ship together; no migration.

## Rollback Strategy

Revert the merge commit.
