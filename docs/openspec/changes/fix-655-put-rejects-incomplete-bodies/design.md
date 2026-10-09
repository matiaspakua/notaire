# Design

## Context

#655 tracks request validation at the boundary. Run 8 left three updates that answered 200 to `{}`; the Owner decided they answer 400 (Oct 9). This PR is stacked on #1371 (and #1370) for the `RequiredFields` and `ErrorResponses` helpers.

## Goals / Non-Goals

Goal: the three PUTs reject empty or incomplete bodies with a field-level 400 and the contract says so. Non-goals: PATCH semantics; changing how absent association ids are handled on `PUT /tramites/{id}` (kept); the testimony `version` handling; the bare testimony create (#1335).

## Decisions

- "Incomplete" = a field the entity cannot be stored without: the POST rule (`idProcedureType`) or a NOT NULL column exposed by the body (`number`, `observed`/`flagged`, `verified`). Nullable columns (`notes`) keep full-replacement semantics.
- `DtoTestimony` uses primitives, so absence is invisible after binding. `TestimonyUpdateRequest` overrides the setters to record presence instead of changing the shared DTO, which is also a response schema.
- The workflow assignment keeps a `Map` body so that a missing key (400) differs from an explicit `null` (unassign). Its documented schema is `WorkflowAssignmentRequest`, with the property required and nullable.
- All missing fields are reported at once (`RequiredFields.check()`), before the 404 lookup, like #1373.
- Owner decision on duplicates (409) checked: `testimonies`, `procedures` and `procedure_types` have no unique constraints, so it does not apply here.

## Riesgos / Trade-offs

A client that sent partial PUT bodies now gets 400. No shipped client does: the UI never calls the testimony or procedure PUT and always sends `workflowDefinitionId`; Bruno and Playwright already send complete bodies.

## Testing Strategy

`IncompletePutBodiesIntegrationTest` (10 cases) observed failing 9/10 first (the complete-body case already passed); `RequiredFieldsTest`; Bruno 04a requests red against main (200); vitest guard for the hook.

## Regression Strategy

Full backend suite; full Bruno run on `notaire_probe`; Playwright TS-0022 (workflow assignment), TS-0021, TS-0012/0031/0032 (testimonies).

## Playwright Strategy

TS-0022, TS-0021, TS-0012, TS-0031, TS-0032.

## Deployment Strategy

Backend only.

## Rollback Strategy

Revert the merge commit.
