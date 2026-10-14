<!-- Governed by CONSTITUTION.md. CU76, issue #1670. Evidence: vitest --coverage on 2026-10-14; mutation check on the folio page. -->

## ADDED Requirements

### Requirement: The testimony movement and folio admin rules are unit tested

The frontend unit suite MUST assert that each testimony status offers one action and calls its endpoint, that a folio in use cannot be edited or deleted, that a folio type edit resends the loaded version, and that a type in use is not offered for deletion. The Vitest coverage floors MUST be at least 69% statements, 62% branches, 65% functions and 70% lines.

#### Scenario: a status offers one action

- **WHEN** testimonies in each status are listed
- **THEN** each row shows only the action its status allows

#### Scenario: a folio in use is protected

- **WHEN** a folio with status Utilizado is listed
- **THEN** its edit and delete buttons are disabled

#### Scenario: a type edit resends the version

- **WHEN** a folio type is edited
- **THEN** the PUT carries the loaded version, notes and enabled flag
