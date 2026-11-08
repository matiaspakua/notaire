# fe-payments-hexagon Specification

## Purpose
TBD - created by archiving change 2026-10-16-refactor-1832-fe-payments-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Payment UI uses core ports only

Payment list/filter/mutation hooks and the payments dashboard page SHALL call composition/use cases. The HTTP gateway SHALL be the only payments path that imports `api-client`. Create payloads SHALL preserve `idBudget`.

#### Scenario: usePayments leaves the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entry for `usePayments.ts`

#### Scenario: Payments dashboard leaves the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entry for `src/app/dashboard/payments/page.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients list, filter by budget/date, create, update, or delete payments
- **THEN** behaviour matches the pre-slice hooks and payments page
