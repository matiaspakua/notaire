<!-- Governed by CONSTITUTION.md. RNF-09, issue #1961. -->

## Purpose

Complete design-system coherence on existing Next.js dashboard surfaces left after epic #1941 waves 1–7.

## ADDED Requirements

### Requirement: List query failures use ErrorState retry

Dashboard pages that render a DataTable fed by react-query SHALL pass `error` and `onRetry` when the query fails.

#### Scenario: Concepts list failure shows retry

- **WHEN** the concepts query fails
- **THEN** DataTable shows ErrorState with a retry control that refetches

### Requirement: Secondary forms validate required fields

Create/edit dialogs on secondary modules SHALL use `requiredFieldErrors` and FormField `error` props for required inputs before calling the API.

#### Scenario: Copy create blocks empty number

- **WHEN** a user saves a copy without a number
- **THEN** the number FormField shows a required error and no create mutation runs

### Requirement: REST client error doc

Permanent documentation SHALL describe frontend REST error handling and retry (#293).

#### Scenario: Doc exists under docs/300-development

- **WHEN** the Wave 8 change is merged
- **THEN** `docs/300-development/frontend-rest-client-errors.md` exists and is linked from the design system
