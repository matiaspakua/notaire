# ui-page-chrome — delta (#2071)

## Purpose

Extend page-chrome empty invitations so shared tables can teach the next step.

## ADDED Requirements

### Requirement: DataTable empty state forwards description and action

The system SHALL allow `DataTable` callers to pass an optional empty description
and one optional empty action. When the table (or mobile card layout) has no
rows and is not loading or in error, `EmptyState` SHALL render that description
and action without the page reimplementing empty layout.

#### Scenario: DataTable empty shows description and action

- **GIVEN** a DataTable with empty data, an `emptyDescription`, and an `emptyAction`
- **WHEN** it renders
- **THEN** the empty-state description text is visible
- **AND** the empty-state action control is visible

### Requirement: Pilot list empties teach the next create step

People, managements, and budgets list pages SHALL pass an empty description to
`DataTable`. When the page already exposes a create action, that same create
handler SHALL be offered as the empty action. Pages MUST NOT invent create flows
that do not already exist.

#### Scenario: People list empty shows description

- **GIVEN** the people list has no rows to show
- **WHEN** the page renders the empty table
- **THEN** the empty-state description for people is visible
