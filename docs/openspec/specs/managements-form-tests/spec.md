# managements-form-tests Specification

## Purpose
TBD - created by archiving change test-1676-managements-form-tests. Update Purpose after archive.

## Requirements

### Requirement: The managements form, transition and client filter are unit tested

The frontend unit suite MUST assert that creating a management sends the chosen values and leaves out an empty property, that editing prefills from the complete case, locks the status and keeps heading and notes, that a redirect note is announced only when the save added it, that a transition offers only the named destinations of the current step, and that the client filter replaces the paged list.

#### Scenario: edit keeps the status locked

- **WHEN** a management is edited
- **THEN** the status select is disabled and the values come from the complete case

#### Scenario: transition destinations

- **WHEN** the change state dialog opens
- **THEN** only the named destinations of the current step are offered
