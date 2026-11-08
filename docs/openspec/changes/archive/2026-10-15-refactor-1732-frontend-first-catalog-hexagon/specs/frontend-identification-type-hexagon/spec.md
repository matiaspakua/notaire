## ADDED Requirements

### Requirement: Identification types use a frontend hexagon

The identification-type catalog MUST live under `frontend/src/core/identification-type` with pure domain and application ports/use cases. An HTTP adapter under `adapters/http/identification-type` MUST implement the outbound gateway using `api-client`. Hooks and UI for this catalog MUST NOT import `@/lib/api-client`. Composition MUST wire the adapter to the use cases. Existing admin and person flows MUST keep the same behaviour.

#### Scenario: core has no framework or transport imports

- **WHEN** the identification-type core modules are linted
- **THEN** they do not import React, Next, TanStack, adapters, or lib

#### Scenario: hooks and admin page leave the lint baseline

- **WHEN** eslint-suppressions.json is checked after the slice
- **THEN** `useIdentificationTypes.ts` and the identification-types admin page are absent from the suppressions file

#### Scenario: catalog CRUD behaviour unchanged

- **WHEN** a user lists, creates, updates, or deletes an identification type (or people pick a type)
- **THEN** the same API paths and UI outcomes occur as before the refactor

#### Scenario: architecture pin test records the shrink

- **WHEN** architecture-boundaries Vitest runs
- **THEN** it asserts the identification-type UI files are not suppressed and adapters/http may still import api-client
