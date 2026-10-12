# workflow-editor-editing — delta

## Purpose

Adding, connecting, editing and deleting workflow statuses and transitions through visible controls.

## MODIFIED Requirements

### Requirement: Visual editing with explicit controls

The workflow editor SHALL let an admin add, connect, retype and delete statuses and transitions through visible controls, SHALL ask for confirmation before every delete, and SHALL reject self-loop and duplicate transitions with a message.

#### Scenario: Panel

- **WHEN** a status is selected
- **THEN** type, outgoing and incoming transitions are shown

#### Scenario: Retype

- **WHEN** the type select changes
- **THEN** PUT /workflow-node/{id} with the new type

#### Scenario: Add transition

- **WHEN** destination chosen in the panel
- **THEN** the transition is created with its description

#### Scenario: Duplicate

- **WHEN** the same pair again
- **THEN** rejected with a message, no API call

#### Scenario: Confirm delete

- **WHEN** delete is clicked
- **THEN** nothing happens until confirmed

#### Scenario: No double-click delete

- **WHEN** a box is double-clicked
- **THEN** it stays
