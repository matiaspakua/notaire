<!-- Governed by CONSTITUTION.md. CU76, issue #1672. Evidence: vitest --coverage on 2026-10-14; mutation checks on both pages. -->

## ADDED Requirements

### Requirement: Workflow activation and protocol report rules are unit tested

The frontend unit suite MUST assert that a new workflow cannot be saved as active, that an edited workflow becomes active only after the backend validates it and an invalid one shows localized problems, and that each protocol report offers only its supported filters and requests its own URL. The Vitest coverage floors MUST be at least 72% statements, 63% branches, 67% functions and 73% lines.

#### Scenario: a new workflow cannot be active

- **WHEN** a new workflow is saved with the active flag
- **THEN** it is not created and the missing nodes are reported

#### Scenario: an invalid workflow stays inactive

- **WHEN** the backend reports an edited workflow invalid
- **THEN** no update is sent and the problems are listed with a link to the editor

#### Scenario: a report offers its own filters

- **WHEN** the tax statement dialog is open
- **THEN** the notary and folio type filters are absent and the year and month are sent
