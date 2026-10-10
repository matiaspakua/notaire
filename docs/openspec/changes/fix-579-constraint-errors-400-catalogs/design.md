# Design

## Context

Slice 1 of #579 (#1331) removed raw exception text from controller errors without changing status codes. Slice 2 aligns the status of constraint errors with GlobalExceptionHandler, one controller group per PR; this PR covers the five catalog controllers.

## Goals / Non-Goals

Goal: catalog constraint errors answer 400. Non-goals: the other controller groups (next PRs), removing the per-controller try/catch, DELETE in-use conflicts (stay 409), field-level bean validation of catalog names (#655).

## Decisions

Every SQLState class 23 error counts. Owner decision (Oct 9): a unique violation (SQLState 23505, Hibernate `ConstraintKind.UNIQUE`, `DuplicateKeyException`) is a duplicate of stored data and answers 409; the rest (NOT NULL, foreign key, check) answer 400. `GlobalExceptionHandler` applies the same rule through `ErrorResponses.isUniqueViolation`, so both paths agree. DELETE keeps 409 for referenced rows: the request is well formed and conflicts with stored state. Concept updates are partial (null fields are kept), so they rarely hit a constraint; the helper still covers them.

## Riesgos / Trade-offs

A client that treated 409 as 'invalid catalog data' now sees 400; the shipped frontend shows the message for any non-2xx. Endpoints that relied on `GlobalExceptionHandler` for duplicates (role rename, second registration draft for a deed, notebook number race) move from 400 to 409; the UI shows the message for any non-2xx.

## Testing Strategy

`CatalogConstraintErrorsIntegrationTest` (15 cases) observed failing first (409 on create, 500 on update, PUT without 400 in the contract); Bruno `08-create-empty-body` red against main (409).

## Regression Strategy

Full backend suite; full Bruno run against this backend (334/334 requests, 568 tests); Playwright chromium TS-0024, TS-0029, tipo-documento-vencimiento-config, presupuesto-catalogo-items (36 passed).

## Playwright Strategy

TS-0024, TS-0029, tipo-documento-vencimiento-config, presupuesto-catalogo-items: 36 passed.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
