## ADDED Requirements

### Requirement: Protocol UI uses core ports only

Protocol home, notebooks, and auxiliary pages SHALL call composition/use cases. HTTP gateways SHALL be the only paths that import `api-client`. Folio list/types/notaries SHALL reuse existing folio composition where possible.

#### Scenario: Protocol pages leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `protocol/page.tsx`, `protocol/notebooks/page.tsx`, or `protocol/auxiliary/page.tsx`

#### Scenario: REST behaviour unchanged
- **WHEN** clients list folios, create notebooks, download covers/reports, or start auxiliary deeds
- **THEN** behaviour matches the pre-slice pages