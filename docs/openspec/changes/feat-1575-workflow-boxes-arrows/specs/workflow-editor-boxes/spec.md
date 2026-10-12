# workflow-editor-boxes — delta

## Purpose

Drawing workflow statuses as boxes and transitions as arrows.

## MODIFIED Requirements

### Requirement: Status boxes and transition arrows

The workflow graph SHALL draw each status as a fixed-width box with a text type badge and a distinct shape for start and final nodes, SHALL draw each transition as an arrow with a closed head and a short readable label, and SHALL highlight the selected box.

#### Scenario: Badge

- **WHEN** a start and a final node
- **THEN** badges read Inicial and Final; shapes differ

#### Scenario: Arrows

- **WHEN** the seeded transitions
- **THEN** every edge path has an arrow marker

#### Scenario: Labels

- **WHEN** a long description
- **THEN** truncated with an ellipsis; full text kept

#### Scenario: Fit

- **WHEN** the editor opens
- **THEN** every box is inside the canvas

#### Scenario: Selection

- **WHEN** edit mode, a box is clicked
- **THEN** the box is highlighted
