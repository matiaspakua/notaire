# ui-page-chrome Specification

## Purpose
Shared page chrome primitives for Notaire dashboard surfaces.

## Requirements

### Requirement: PageHeader presents title and optional actions

The system SHALL provide a `PageHeader` that renders a page title, an optional
short supporting description, and an optional actions slot, using design-system
tokens. Decorative ALL-CAPS labels SHALL NOT be used in the header typography.

#### Scenario: PageHeader with actions

- **GIVEN** a PageHeader with title and actions
- **WHEN** it renders
- **THEN** the title is visible as a heading
- **AND** the actions content is visible

### Requirement: EmptyState invites a next action

The system SHALL provide an `EmptyState` that shows a title and MAY show a
description and a primary action control.

#### Scenario: EmptyState with action

- **GIVEN** an EmptyState with title and action
- **WHEN** it renders
- **THEN** the title and action are visible

### Requirement: PageSection groups related content

The system SHALL provide a `PageSection` with a title and children content using
token spacing.

#### Scenario: PageSection renders children

- **GIVEN** a PageSection with a title and child content
- **WHEN** it renders
- **THEN** the title and children are visible

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
