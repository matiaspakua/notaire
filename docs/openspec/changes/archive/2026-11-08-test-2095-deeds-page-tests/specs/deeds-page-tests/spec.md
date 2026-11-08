<!-- Governed by CONSTITUTION.md. CU76, issue #2095. Evidence: vitest --coverage on the deeds page (branches 90); four mutation checks. -->

## ADDED Requirements

### Requirement: The deeds page rules are unit tested

The frontend unit suite MUST assert that a deed needs a number and a date and, on create only, a folio, that only new folios are offered, that only an unsigned deed offers signing and only after a confirmation, and that a number is looked up on the server.

#### Scenario: only an unsigned deed can be signed

- **WHEN** the list shows a signed deed and an unsigned deed
- **THEN** only the unsigned one offers the sign action

#### Scenario: the folio is required only on create

- **WHEN** a deed is edited without choosing a folio
- **THEN** the save is sent
