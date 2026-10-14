# workflow-tracker-tests Specification

## Purpose
TBD - created by archiving change test-1675-workflow-tracker-tests. Update Purpose after archive.

## Requirements

### Requirement: The workflow tracker interactions are unit tested

The frontend unit suite MUST assert that a step opens its detail modal by click, Enter or Space, that the modal closes by its button, Escape or the backdrop but not by a click inside the dialog, that the re-entry badge and movement timeline appear only on the inscription step, and that an unknown status is shown as pending.

#### Scenario: Escape closes the modal

- **WHEN** a step is open and another key is pressed
- **THEN** the modal stays open until Escape is pressed

#### Scenario: re-entry only on the inscription step

- **WHEN** the trace has returned movements
- **THEN** only the inscription step shows the badge and the timeline
