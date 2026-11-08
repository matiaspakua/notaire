## ADDED Requirements

### Requirement: People frontend is hexagonal

The `usePeople` and `usePersonSearch` hooks MUST NOT import `@/lib/api-client`. Domain and application under `frontend/src/core/person` MUST stay free of React/Next/api-client. The HTTP adapter under `adapters/http/person` MUST be the only people module that calls api-client. ESLint suppressions for the moved files MUST be removed; the shrink-only baseline pin MUST tighten.

#### Scenario: list and search people through use cases

- **WHEN** a hook lists a page or searches people
- **THEN** the call goes through composition → use cases → HTTP gateway

#### Scenario: create update delete

- **WHEN** hooks create, update, or delete a person
- **THEN** those operations use the gateway via use cases without importing api-client in the hook

#### Scenario: suppressions shrink

- **WHEN** architecture-boundaries tests run
- **THEN** usePeople and usePersonSearch are absent from eslint-suppressions.json
