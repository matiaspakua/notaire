<!-- Governed by CONSTITUTION.md. CU76, issue #1677. Evidence: vitest --coverage on the editor page (branches 74.31); four mutation checks. -->

## ADDED Requirements

### Requirement: The workflow editor page actions are unit tested

The frontend unit suite MUST assert that the editing controls appear only in edit mode, that adding a status needs a choice, that a transition is checked before it is sent, that a delete asks first, that a description is saved with the transition's endpoints, and that the server validation report is shown.

#### Scenario: a duplicate transition is refused

- **WHEN** a transition that already exists is added from the list
- **THEN** the problem is shown and nothing is sent

#### Scenario: adding a status needs a choice

- **WHEN** the add status dialog is confirmed without a status
- **THEN** an error is shown and nothing is sent
