# Design

## Context

Slice 2 of #579 (#1370) introduced `ErrorResponses.createFailed`/`updateFailed` for the catalog controllers. This slice covers the workflow controllers, where #655's empty-body probe also found 500s (missing ids) and an accepted empty node update.

## Goals / Non-Goals

Goal: missing or invalid workflow data answers 400. Non-goals: testimony and person controllers (next PRs), bean validation on the shared DTOs (would change response schemas), unique-violation status (stays 400 like #1370 until the Owner decides).

## Decisions

- Required-field checks are programmatic (`RequiredFields`) and run before the repository calls and the `try` blocks; a missing field answers 400 before a 404 for an unknown id.
- The 400 responses name the required fields in their OpenAPI description, since the shared DTO schema cannot mark them required without also marking the response schemas.
- Transition `PUT` keeps 409 for non-constraint failures (it used 409 before), via `createFailed`.
- An empty node update is rejected: the UI always sends a position, and a 200 that changes nothing hides client bugs.

## Riesgos / Trade-offs

A client sending `{}` to `PUT /workflow-node/{id}` now gets 400 instead of a silent 200.

## Testing Strategy

`WorkflowConstraintErrorsIntegrationTest` (17 cases) observed failing 16/17 first (409, 500 and 200 instead of 400; PUT without 400 in the contract); the position-only update case pins the UI path.

## Regression Strategy

Full backend verify; full Bruno run against this backend; Playwright workflow editor specs.

## Playwright Strategy

Workflow editor specs (TS-0076/TS-0083 area).

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
