# fe-managements-hexagon Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1839-fe-managements-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Management UI uses core ports only

Management hooks (`useManagements`, `useManagementSearch`, `useManagementSummary`) and the managements dashboard SHALL call composition/use cases. The HTTP gateway SHALL be the only path that imports `api-client` for this surface. Complete-case payloads SHALL map `procedureTypeId` to wire `typeProcedureId`. Archive SHALL keep the `confirmado` query flag.

#### Scenario: Management hooks leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `useManagements.ts`, its wire/complete-case tests, `useManagementSearch.ts`, or `useManagementSummary.ts` (+ test)

#### Scenario: Managements pages leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for managements `page.tsx` or `ManagementCurrentStatus.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients page, create, complete-case edit, archive, transition, or search managements
- **THEN** behaviour matches the pre-slice hooks
