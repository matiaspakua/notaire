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

