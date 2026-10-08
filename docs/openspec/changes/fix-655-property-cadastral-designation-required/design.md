# Design

## Context

Issue #655 lists endpoints that accept incomplete bodies. Run 7: the Owner decided inmuebles require `cadastralDesignation`. On PostgreSQL the `properties.nomenclature` column is nullable, so the API accepted `{}`; the entity declares it `optional = false` and the UI labels it required.

## Goals / Non-Goals

Goal: no new property without a designation. Non-goals: properties already stored without one (none in the seed data); other optional property fields; a NOT NULL migration.

## Decisions

Apply the rule to PUT as well because PUT is a full replacement through the same record: leaving it optional there would let an update erase the designation. `@NotBlank` rather than `@NotNull` because an empty or blank designation identifies nothing.

## Riesgos / Trade-offs

An external client that created or replaced properties without a designation now gets 400; none ships in this repository. A legacy row without a designation must be given one when edited, which the form already asks for.

## Testing Strategy

`PropertyRequestValidationIntegrationTest` (7 cases) and Playwright CU69-GW04 written first and observed failing (all 7 cases; Save enabled with a blank designation).

## Regression Strategy

Full backend suite; full Bruno run (325 requests, 551 tests) against this backend on PostgreSQL 17; Playwright chromium TS-0019, TS-0082, TS-0090 (13 passed) against this frontend and backend.

## Playwright Strategy

TS-0019 (incl. CU69-GW04), TS-0082, TS-0090: 13 passed.

## Deployment Strategy

Backend and frontend together.

## Rollback Strategy

Revert the merge commit.
