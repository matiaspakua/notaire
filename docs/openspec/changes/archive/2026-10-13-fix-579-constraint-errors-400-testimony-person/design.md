# Design

## Context

#1370 (slice 2) added `ErrorResponses.createFailed`/`updateFailed` for the catalogs; #1371 (slice 3) applied them to the workflow controllers. This slice covers the remaining controllers with their own catch-all on POST/PUT: testimony, testimony movement and person.

## Goals / Non-Goals

Goal: constraint errors on these creates and updates answer 400. Non-goals: required-field rules (#655, the bare testimony create is #1335), unique-violation status (stays 400 like #1370 until the Owner decides), DELETE in-use conflicts (stay 409).

## Decisions

- Person update keeps 409 for non-constraint failures (it used 409), so it uses `createFailed`; `DuplicatePersonException` is caught first and keeps its 409 body.
- The unit test builds each controller from its public constructor with mocks for any collaborator it doesn't configure, so it keeps working when #1335 adds the `DeedRepository` dependency to `TestimonyController`.

## Riesgos / Trade-offs

A client that treated 409 as "invalid data" on these endpoints now sees 400; the shipped frontend shows the message for any non-2xx.

## Testing Strategy

`TestimonyPersonConstraintErrorsTest` (7 cases) and `TestimonyPersonConstraintErrorsIntegrationTest` (4 cases) observed failing 10/11 first (409/500 instead of 400, PUT without 400 in the contract); the other-failures case pins the unchanged statuses.

## Regression Strategy

Full backend verify; full Bruno run against this backend; Playwright testimony and people specs.

## Playwright Strategy

Testimony and people specs.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
