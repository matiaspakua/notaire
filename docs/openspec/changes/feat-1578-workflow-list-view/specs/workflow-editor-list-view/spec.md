# workflow-editor-list-view — delta

## Purpose

A table-based, keyboard-operable view of the workflow with the same edit actions as the graph.

## MODIFIED Requirements

### Requirement: Workflow list view

The workflow editor SHALL offer a list view in which statuses and transitions can be added, retyped, described and deleted with the keyboard only, every control having an accessible name.

#### Scenario: Keyboard build

- **WHEN** list view, keyboard only
- **THEN** start → mid → final built and valid

#### Scenario: Names

- **WHEN** the list view
- **THEN** every button, select and text field has an accessible name

#### Scenario: Confirm

- **WHEN** delete in the list
- **THEN** asks for confirmation

#### Scenario: Duplicate

- **WHEN** same pair again
- **THEN** rejected with a message

#### Scenario: Box name

- **WHEN** a focused box
- **THEN** name includes outgoing count
