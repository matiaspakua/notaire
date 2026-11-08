## ADDED Requirements

### Requirement: Procedures admin and procedure-type hooks are hexagonal

The procedures administration page and `useProcedureTypes` hooks MUST NOT import `@/lib/api-client`. The HTTP adapter under `adapters/http/procedure-type` MUST be the only module for this surface that calls api-client. ESLint suppressions for the moved files MUST be removed.

#### Scenario: procedure-type hooks via use cases
- **WHEN** hooks list, search, mutate, check in-use, or assign workflow
- **THEN** calls go through composition → use cases → HTTP gateway

#### Scenario: procedures page via use cases
- **WHEN** the admin searches or checks in-use before delete
- **THEN** operations use composition-wired use cases without importing api-client in the page

#### Scenario: suppressions shrink
- **WHEN** architecture-boundaries tests run
- **THEN** procedures page, RequiredDocumentsDialog, and useProcedureTypes are absent from eslint-suppressions.json