# testimony-manual-registration — delta

## Purpose

Registering a testimony manually from the UI.

## MODIFIED Requirements

### Requirement: Manual testimony registration

The testimonies page SHALL let the user register a testimony for a chosen deed with number, notes and verified through POST /testimonies, SHALL show missing-value and 400 field errors next to their fields, SHALL toast 404 and 409, and SHALL refresh the list on success.

#### Scenario: Register

- **WHEN** a signed deed and a number
- **THEN** the testimony is stored and listed

#### Scenario: Required

- **WHEN** no deed or no number
- **THEN** inline errors, no request

#### Scenario: 400

- **WHEN** the API answers `number: …`
- **THEN** the message shows under Número

#### Scenario: 404

- **WHEN** the deed is gone
- **THEN** 'La escritura ya no existe' toast

#### Scenario: Mobile

- **WHEN** 390px
- **THEN** the flow works
