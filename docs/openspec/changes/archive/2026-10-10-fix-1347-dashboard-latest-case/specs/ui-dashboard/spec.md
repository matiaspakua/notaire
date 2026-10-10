# ui-dashboard — delta

## Purpose

The dashboard landing page.

## MODIFIED Requirements

### Requirement: Dashboard hero follows the newest traceable case

The dashboard hero SHALL show the workflow of the newest management that has one (among the 20 newest), SHALL name it by number and header, SHALL show an empty state with a link to the managements list when none has one, and SHALL NOT render controls without an action.

#### Scenario: Newest traced case

- **WHEN** a management with a workflow is created
- **THEN** the hero shows it or a newer one, never the oldest

#### Scenario: Skips cases without a workflow

- **WHEN** the newest managements answer 400 for their trace
- **THEN** the hero shows the newest one that answers 200

#### Scenario: Empty state

- **WHEN** none of the newest managements has a workflow
- **THEN** an empty state links to /dashboard/gestiones

#### Scenario: No dead button

- **WHEN** the dashboard renders
- **THEN** there is no 'view all' button
