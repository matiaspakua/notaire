## ADDED Requirements

### Requirement: Deed UI uses core ports only

Deed list/search/mutation hooks and the deeds dashboard page SHALL call composition/use cases. The HTTP gateway SHALL be the only deeds path that imports `api-client`. Folio list for the deeds form SHALL use existing folio composition.

#### Scenario: useDeeds and useDeedSearch leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `useDeeds.ts` or `useDeedSearch.ts`

#### Scenario: Deeds dashboard leaves the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entry for `src/app/dashboard/deeds/page.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients page, search, create, update, delete, or sign deeds
- **THEN** behaviour matches the pre-slice hooks and deeds page