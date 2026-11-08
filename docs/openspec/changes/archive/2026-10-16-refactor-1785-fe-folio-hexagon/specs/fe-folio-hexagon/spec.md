## ADDED Requirements

### Requirement: Folios admin uses ports and HTTP adapters

The folios administration page MUST obtain folio-type and folio instance data through composition-wired use cases. Direct imports of `@/lib/api-client` from the admin page MUST be removed. Domain and application under `frontend/src/core/folio-type` and `frontend/src/core/folio` MUST stay free of React/Next/api-client. The HTTP adapters under `adapters/http/folio-type` and `adapters/http/folio` MUST be the only modules for this surface that call api-client. ESLint suppressions for the folios admin page MUST be removed; the shrink-only baseline pin MUST tighten.

#### Scenario: Folio-type catalog via use cases

- **WHEN** the admin lists, searches, creates, updates, deletes, or checks in-use for folio types
- **THEN** those operations go through `folioTypeUseCases` backed by an HTTP gateway

#### Scenario: Folio instances via use cases

- **WHEN** the admin lists, searches by status, creates, updates, or deletes folios, or loads available notaries
- **THEN** those operations go through `folioUseCases` backed by an HTTP gateway

#### Scenario: Suppressions shrink

- **WHEN** architecture-boundaries tests run
- **THEN** `src/app/dashboard/administration/folios/page.tsx` is absent from eslint-suppressions.json
