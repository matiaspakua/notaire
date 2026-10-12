# workflow-editor-mobile — delta

## Purpose

The workflow editor's behaviour on small screens.

## MODIFIED Requirements

### Requirement: Workflow editor on a phone

Below the md breakpoint the workflow editor SHALL open in the list view, SHALL keep the diagram read-only, and SHALL not overflow the page horizontally.

#### Scenario: Default

- **WHEN** 390px
- **THEN** list view selected

#### Scenario: No overflow

- **WHEN** 390px, view and edit mode
- **THEN** no horizontal scroll; header buttons inside the viewport

#### Scenario: Edit

- **WHEN** 390px, edit mode
- **THEN** a transition is added in the list

#### Scenario: Read-only graph

- **WHEN** 390px, diagram tab
- **THEN** no draggable boxes; note shown
