# workflow-editor-validation — delta

## Purpose

Showing workflow consistency problems on the canvas and gating activation on them.

## MODIFIED Requirements

### Requirement: Canvas validation and activation gate

The workflow editor SHALL mark on the canvas every status involved in a consistency problem with a localized message, and the workflows list SHALL refuse to save a workflow as active while it is invalid, listing the problems.

#### Scenario: Two starts

- **WHEN** two INITIAL statuses
- **THEN** both marked, list names them

#### Scenario: No final

- **WHEN** no FINAL status
- **THEN** problem listed

#### Scenario: Isolated

- **WHEN** a status unreachable from the start
- **THEN** it is marked 'No alcanzable desde el inicio'

#### Scenario: Activation blocked

- **WHEN** saving an invalid workflow as active
- **THEN** not saved; problems shown

#### Scenario: Activation allowed

- **WHEN** the problem is fixed
- **THEN** saved as active
