## ADDED Requirements

### Requirement: Management-status frontend is hexagonal

The management-status admin page and `useManagementStatuses` hook MUST NOT import `@/lib/api-client`. Domain and application under `frontend/src/core/management-status` MUST stay free of React/Next/api-client. The HTTP adapter under `adapters/http/management-status` MUST be the only catalog module that calls api-client. ESLint suppressions for the moved files MUST be removed; the shrink-only baseline pin MUST tighten.

#### Scenario: list statuses through use cases

- **WHEN** the hook or page lists management statuses
- **THEN** the call goes through composition → use cases → HTTP gateway

#### Scenario: create update delete and search

- **WHEN** the admin page creates, updates, deletes, searches, or checks in-use
- **THEN** those operations use the gateway via use cases without importing api-client in the page

#### Scenario: suppressions shrink

- **WHEN** architecture-boundaries tests run
- **THEN** management-statuses page and useManagementStatuses are absent from eslint-suppressions.json
