> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Backend identification types are ports-only (#1700). FE lint boundaries exist (#1696) with 66 suppressions. Plan F1 / assessment: first catalog into core/+adapters/http.

## Goals / Non-Goals

**Goals:** First real FE hexagon for identification types; suppressions shrink by two; English identifiers; no behaviour change.
**Non-Goals:** Migrating other catalogs; full composition DI framework; CHANGELOG while #1766 is open.

## Decisions

1. Pick identification types (backend-clean, dedicated admin page + people consumer). 2. Gateway port + thin use-case functions (list/create/update/delete) per ADR-030 FE skeleton. 3. Module-level composition export (swap in tests via gateway fakes at core layer). 4. Extract ApiError to lib/api-error so pages can keep 409 handling without importing api-client. 5. Skip CHANGELOG Unreleased while Release Please #1766 owns it.

## Riesgos / Trade-offs

- [Page still needs ApiError for 409] → extract ApiError to lib/api-error; re-export from api-client.
- [Hook tests mock fetch] → keep http gateway on api-client so fetch mocks still work.
- [Release Please CHANGELOG conflict] → omit Unreleased entry.

## Testing Strategy

Vitest: core use cases with fake gateway; http gateway mocked api-client; architecture-boundaries pin; identification-types-admin + people-identification-type stay green. Playwright TS-0139 if UI wiring changes.

## Regression Strategy

cd frontend && npm test -- architecture-boundaries identification-types-admin people-identification-type useIdentificationTypes

## Playwright Strategy

Existing TS-0139 identification-type catalog covers admin CRUD; no new E2E if behaviour unchanged — re-run when CI Playwright runs.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
