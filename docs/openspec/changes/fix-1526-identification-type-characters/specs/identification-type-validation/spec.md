# identification-type-validation — delta

## Purpose

The identification-type API validates the number length.

## MODIFIED Requirements

### Requirement: Identification type number length

The API SHALL reject an identification type whose `characters` is missing, blank, or not a whole number from 1 to 50 with 400, and the admin form SHALL apply the same rule.

#### Scenario: Missing

- **WHEN** a POST or PUT without `characters`
- **THEN** the API answers 400, not 500

#### Scenario: Not a number

- **WHEN** `characters` is "abc", "0", "51" or "007"
- **THEN** the API answers 400 and stores nothing

#### Scenario: Valid

- **WHEN** `characters` is 1, 8, 11 or 50
- **THEN** the type is created

#### Scenario: Form

- **WHEN** an admin types 51 in the form
- **THEN** the form shows the 1-to-50 message and sends nothing
