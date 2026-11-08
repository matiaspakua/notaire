# fe-concept-hexagon Specification

## Purpose
TBD - created by archiving change refactor-1781-fe-concept-hexagon. Update Purpose after archive.

## Requirements

### Requirement: Concept frontend is hexagonal

The concepts admin page and `useConcepts` hooks MUST NOT import `@/lib/api-client`. Domain and application under `frontend/src/core/concept` MUST stay free of React/Next/api-client. The HTTP adapter under `adapters/http/concept` MUST be the only catalog module that calls api-client. ESLint suppressions for the moved files MUST be removed; the shrink-only baseline pin MUST tighten.

#### Scenario: list concepts through use cases

- **WHEN** the hook or page lists concepts
- **THEN** the call goes through composition → use cases → HTTP gateway

#### Scenario: create update delete search and in-use

- **WHEN** the admin page creates, updates, deletes, searches, or checks in-use
- **THEN** those operations use the gateway via use cases without importing api-client in the page

#### Scenario: suppressions shrink

- **WHEN** architecture-boundaries tests run
- **THEN** concepts page and useConcepts are absent from eslint-suppressions.json
