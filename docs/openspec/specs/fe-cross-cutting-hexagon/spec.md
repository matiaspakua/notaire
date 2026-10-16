# fe-cross-cutting-hexagon Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1843-fe-cross-cutting-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Cross-cutting UI uses core ports only

Users, roles, audit, reports, registration-draft, login, and sidebar logout SHALL call composition/use cases. HTTP gateways SHALL be the only paths that import `api-client` for these surfaces. Reports page SHALL reuse folio/folio-type composition for notaries and folio types.

#### Scenario: Cross-cutting files leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for the eight cross-cutting files

#### Scenario: REST behaviour unchanged
- **WHEN** clients manage users/roles, page audit, download reports, use registration drafts, log in or out
- **THEN** behaviour matches the pre-slice code
