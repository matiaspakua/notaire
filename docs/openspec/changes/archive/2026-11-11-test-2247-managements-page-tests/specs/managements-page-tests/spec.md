## ADDED Requirements

### Requirement: The managements page rules are unit tested

The frontend unit suite MUST assert that a management needs number, budget, notary, status and procedure type, that an archive conflict is confirmed again and forgotten when dismissed, that a folder is put on hold only with a reason, and that load errors retry the query that failed.

#### Scenario: required fields gate the save

- **WHEN** a new management is saved empty
- **THEN** five required-field errors show and nothing is sent

#### Scenario: an archive conflict is forgotten on dismissal

- **WHEN** the archive conflict dialog is dismissed and reopened
- **THEN** the normal confirmation is shown
