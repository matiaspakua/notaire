# fe-templates-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1788-fe-templates-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Templates admin and procedure-template hooks are hexagonal

The templates administration page and `useProcedureTemplate` hooks MUST NOT import `@/lib/api-client`. HTTP adapters MUST be the only modules for this surface that call api-client. ESLint suppressions for the moved files MUST be removed.

#### Scenario: procedure-template hooks via use cases
- **WHEN** hooks list or mutate procedure templates
- **THEN** calls go through composition → use cases → HTTP gateway

#### Scenario: templates page via use cases
- **WHEN** the admin manages budget templates or document-cost templates, or loads catalog lookups
- **THEN** operations use composition-wired use cases without importing api-client in the page

#### Scenario: suppressions shrink
- **WHEN** architecture-boundaries tests run
- **THEN** templates page and useProcedureTemplate entries are absent from eslint-suppressions.json
