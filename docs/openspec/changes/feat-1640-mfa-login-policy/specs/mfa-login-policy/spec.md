## ADDED Requirements

### Requirement: Login second-factor policy

The system SHALL decide after a valid password check whether to issue a session, an MFA challenge or an enrolment-required response, based on `notaire.mfa.policy` (OFF, OPTIONAL default, ENFORCE), the user's role and whether a confirmed enrolment exists.

#### Scenario: Enrolled user receives a challenge instead of a token

- **WHEN** a user with a confirmed enrolment logs in with a valid password and policy is OPTIONAL or ENFORCE
- **THEN** the response has valid=false, mfaRequired=true and a challenge token, and no session token or cookie

#### Scenario: Optional policy does not force enrolment

- **WHEN** an administrator without enrolment logs in and policy is OPTIONAL
- **THEN** a session is issued as before

#### Scenario: Enforced policy requires enrolment for admin and notary

- **WHEN** an administrator or notary without a confirmed enrolment logs in and policy is ENFORCE
- **THEN** the response is mfaEnrollmentRequired=true with an enrolment-scoped token and no session

#### Scenario: Other roles are never forced

- **WHEN** a user of another role without enrolment logs in and policy is ENFORCE
- **THEN** a session is issued

#### Scenario: Policy OFF skips the challenge

- **WHEN** policy is OFF
- **THEN** login issues a session even for enrolled users

### Requirement: Second-factor verification

The system SHALL issue the session only after a valid TOTP or recovery code is presented with an unexpired challenge token.

#### Scenario: Valid TOTP completes login

- **WHEN** a valid challenge token and a valid TOTP code are posted to login/mfa
- **THEN** the session token and cookie are issued and the failure counter is cleared

#### Scenario: Recovery code completes login

- **WHEN** a valid challenge token and an unused recovery code are posted
- **THEN** the session is issued and the recovery code is consumed

#### Scenario: Wrong code is rejected and counted

- **WHEN** a wrong code is posted
- **THEN** the response is valid=false, the failure counts toward lockout and no code value is logged

#### Scenario: Locked account is refused

- **WHEN** the account is locked by failed attempts
- **THEN** login/mfa answers 429 without evaluating the code

#### Scenario: Scoped tokens are not sessions

- **WHEN** a challenge or enrolment token is presented as a Bearer credential on a normal endpoint
- **THEN** the request is unauthenticated

### Requirement: Enrolment endpoints

The system SHALL expose enrolment begin, confirm and disable under /api/v1/mfa for the authenticated user only, and disable SHALL require a current TOTP or recovery code.

#### Scenario: Begin and confirm enrolment

- **WHEN** an authenticated user begins enrolment and confirms with a valid code
- **THEN** the secret and otpauth URI are returned once and 10 recovery codes are returned once

#### Scenario: Enrolment-required user can enrol

- **WHEN** the enrolment-scoped token is used on the enrolment endpoints
- **THEN** begin and confirm succeed and the token is refused on every other endpoint

#### Scenario: Wrong confirmation code is counted

- **WHEN** confirm or disable receives a wrong code
- **THEN** the response is 400 and the failure counts toward lockout

#### Scenario: Disable requires a current code

- **WHEN** an enrolled user posts disable with a valid code
- **THEN** the enrolment is removed, and without a valid code it is kept

#### Scenario: Anonymous callers are refused

- **WHEN** an unauthenticated caller reaches an enrolment endpoint
- **THEN** the response is 401
