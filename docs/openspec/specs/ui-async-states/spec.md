# ui-async-states Specification

## Purpose
Shared loading and error chrome for async dashboard surfaces.
## Requirements
### Requirement: LoadingState announces busy content

The system SHALL provide a `LoadingState` that marks itself busy and exposes an
accessible loading label, using design-system skeleton styling.

#### Scenario: LoadingState is busy

- **GIVEN** a LoadingState
- **WHEN** it renders
- **THEN** it has `aria-busy="true"`
- **AND** it exposes a loading label to assistive technology

### Requirement: ErrorState offers an optional retry

The system SHALL provide an `ErrorState` with a title and MAY include a
description and a retry action control.

#### Scenario: ErrorState with retry

- **GIVEN** an ErrorState with title and retry action
- **WHEN** it renders
- **THEN** the title and retry control are visible

### Requirement: DataTable surfaces query errors

When a DataTable is given an error message it SHALL render an ErrorState instead
of an empty-data presentation.

#### Scenario: DataTable shows ErrorState on error

- **GIVEN** a DataTable with `error` set and empty data
- **WHEN** it renders
- **THEN** an ErrorState with that message is visible

