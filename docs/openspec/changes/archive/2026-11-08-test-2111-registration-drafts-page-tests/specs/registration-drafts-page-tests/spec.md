## ADDED Requirements

### Requirement: The registration drafts page rules are unit tested

The frontend unit suite MUST assert that a draft is generated only for a chosen deed, that each step closes its dialog on success and keeps it with the reason on failure, and that every status shows its label.

#### Scenario: a draft needs a deed

- **WHEN** no deed is chosen
- **THEN** the generate action is disabled

#### Scenario: a failed step keeps the dialog

- **WHEN** the registry step is rejected
- **THEN** the reason is reported and the dialog stays open
