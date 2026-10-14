<!-- Governed by CONSTITUTION.md. CU76, issue #1673. Evidence: vitest --coverage on 2026-10-14; mutation checks on the three pages. -->

## ADDED Requirements

### Requirement: Managements actions and catalog page rules are unit tested

The frontend unit suite MUST assert that archiving warns about debt and asks again after a conflict with the confirmation flag, that an archived management offers neither state change nor archive, that a folder on hold needs a reason and only an active folder can be held, that a management status is edited with its version, and that a status or procedure type in use is not offered for deletion. The Vitest coverage floors MUST be at least 74% statements, 65% branches, 68% functions and 75% lines.

#### Scenario: archive asks again after a conflict

- **WHEN** the backend answers 409 to an archive
- **THEN** the dialog shows the conflict and the next confirmation sends the confirmation flag

#### Scenario: a hold needs a reason

- **WHEN** a folder hold is confirmed without a reason
- **THEN** nothing is sent and the reason-required error is shown

#### Scenario: an item in use is not deleted

- **WHEN** a status or procedure type reports in use
- **THEN** a warning is shown and no delete is requested
