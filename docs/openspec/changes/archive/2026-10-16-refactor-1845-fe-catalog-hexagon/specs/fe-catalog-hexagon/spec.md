## ADDED Requirements

### Requirement: Catalog leftover UI uses core ports only

Items, copies, substitutions, and properties hooks SHALL call composition/use cases. HTTP gateways SHALL be the only paths that import `api-client` for these surfaces.

#### Scenario: Catalog leftover hooks leave the eslint baseline
- **WHEN** architecture-boundaries runs after this slice
- **THEN** eslint-suppressions has no entries for `useItems`, `useCopies`, `useSubstitutions`, or `useProperties`

#### Scenario: REST behaviour unchanged
- **WHEN** clients CRUD items/copies/substitutions/properties
- **THEN** behaviour matches the pre-slice hooks