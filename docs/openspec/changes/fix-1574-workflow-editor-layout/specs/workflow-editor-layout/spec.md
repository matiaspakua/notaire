# workflow-editor-layout — delta

## Purpose

Laying out workflow nodes so boxes never overlap.

## MODIFIED Requirements

### Requirement: Non-overlapping workflow layout

The workflow editor SHALL show every node of the loaded workflow without overlapping boxes, SHALL keep saved positions that do not overlap, SHALL place missing or overlapping ones automatically following the transitions, and SHALL save all positions when the user chooses Auto-organizar.

#### Scenario: Seeded workflow

- **WHEN** nodes without positions
- **THEN** every box visible, none overlapping

#### Scenario: Direct load

- **WHEN** the page is opened by URL
- **THEN** the canvas shows the nodes once loaded

#### Scenario: 0,0 nodes

- **WHEN** nodes created at 0,0
- **THEN** laid out without overlap

#### Scenario: Auto-organizar

- **WHEN** edit mode, button pressed
- **THEN** positions saved and kept after reload

#### Scenario: New node

- **WHEN** a node is added
- **THEN** placed right of the existing graph
