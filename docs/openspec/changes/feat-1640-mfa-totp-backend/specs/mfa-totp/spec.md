## ADDED Requirements

### Requirement: TOTP enrolment

The system SHALL let a user begin TOTP enrolment, receiving a generated secret and an otpauth URI, and confirm it with a valid code, at which point 10 single-use recovery codes are issued once.

#### Scenario: Confirm enrolment with a valid code

- **WHEN** a user begins enrolment and confirms with the current valid code
- **THEN** the enrolment is confirmed and 10 recovery codes are returned

#### Scenario: Confirm enrolment with a wrong code

- **WHEN** a user confirms enrolment with an invalid code
- **THEN** the enrolment stays unconfirmed and no recovery codes are issued

#### Scenario: Secret is encrypted at rest

- **WHEN** an enrolment is stored
- **THEN** the persisted secret is ciphertext, never the Base32 secret

### Requirement: TOTP verification

The system SHALL accept a code valid for the current time step or one step either side, and SHALL reject a step already used.

#### Scenario: Valid code accepted

- **WHEN** a confirmed user submits the current code
- **THEN** verification succeeds

#### Scenario: Wrong code rejected

- **WHEN** a confirmed user submits an incorrect code
- **THEN** verification fails

#### Scenario: Replayed code rejected

- **WHEN** a code already accepted is submitted again
- **THEN** verification fails

### Requirement: Recovery codes

The system SHALL accept each recovery code once.

#### Scenario: Recovery code consumed

- **WHEN** a valid recovery code is used
- **THEN** it succeeds once and fails on the second use
