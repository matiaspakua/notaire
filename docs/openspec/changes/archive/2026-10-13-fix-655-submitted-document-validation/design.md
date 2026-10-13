# Design

## Context

Issue #655 tracks controllers that accept invalid input. The payments slice used bean-validation constraints, which `oasdiff` reports as breaking. This slice is semantic validation (date parsing, reference existence), so it is documented with descriptions and responses instead of schema constraints.

## Goals / Non-Goals

Goal: no silent data loss or 500 on submitted documents. Non-goals: other controllers in #655; schema-level constraints (`format: date`, `required`) that would need an accepted-breaking-changes entry.

## Decisions

Keep `date` a plain string in the schema and describe the rule, so `oasdiff` reports no break and no `accepted-breaking-changes.txt` entry is needed. 404 (not 400) for unknown references, matching the existing pattern for missing resources.

## Riesgos / Trade-offs

Clients that sent non-ISO dates now get 400 instead of a document without a date; that was data loss, and the frontend already sends `yyyy-MM-dd` (Playwright green).

## Testing Strategy

Integration test (H2, committed rows) observed failing first: 11 of 12 cases returned 201/200/500.

## Regression Strategy

Full backend suite (2092 tests); Playwright TS-0033, TS-0050, TS-0051, TS-0097 (29 passed) and Bruno `submitted-documents` (11/11) against this backend on PostgreSQL.

## Playwright Strategy

No UI change; Playwright not affected.

## Deployment Strategy

None.

## Rollback Strategy

Revert the commit.
