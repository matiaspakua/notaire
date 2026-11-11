<!-- Governed by CONSTITUTION.md. CU-78, issue #1640. -->

## ADDED Requirements

### Requirement: OWASP A07 mapping matches the shipped MFA control

The OWASP mapping SHALL describe the TOTP second factor as shipped end to end (backend and web UI), cite the automated tests that verify it, and state the default policy honestly.

#### Scenario: mapping cites the E2E test

- **WHEN** the A07 row is read
- **THEN** it names `TS-0046` among the tests and no longer says the UI arrives later

#### Scenario: default policy stated

- **WHEN** the A07 row is read
- **THEN** it states that the default policy is OPTIONAL and that enforcement is an operator switch
