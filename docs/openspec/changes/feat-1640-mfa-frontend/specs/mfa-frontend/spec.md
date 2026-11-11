## ADDED Requirements

### Requirement: Login challenge step

The login page SHALL ask for a second factor when the login response has `mfaRequired=true`, and SHALL create the client session only after `/login/mfa` answers `valid=true`.

#### Scenario: Challenge step is shown instead of a session

- **WHEN** the password step returns `valid=false` and `mfaRequired=true`
- **THEN** the code form is shown, no session is stored and no navigation happens

#### Scenario: Valid TOTP completes the login

- **WHEN** the user submits a 6-digit code
- **THEN** the challenge token and the code are posted to `/users/login/mfa` and, on `valid=true`, the session is stored and the user goes to the dashboard

#### Scenario: Recovery code completes the login

- **WHEN** the user chooses the recovery code option and submits a code
- **THEN** it is posted as `recoveryCode` and not as `code`

#### Scenario: Wrong code is reported and the step stays

- **WHEN** `/users/login/mfa` answers `valid=false`
- **THEN** an error is shown, the code field is cleared and the challenge step remains

#### Scenario: Locked account is reported

- **WHEN** `/users/login/mfa` answers 429
- **THEN** the lockout message is shown

#### Scenario: Malformed code is not sent

- **WHEN** the TOTP field does not hold exactly 6 digits
- **THEN** a validation message is shown and no request is made

### Requirement: Enrolment required at login

The login page SHALL start enrolment with the enrolment-scoped token when the login response has `mfaEnrollmentRequired=true`.

#### Scenario: Enrolment-required user enrols

- **WHEN** the response has `mfaEnrollmentRequired=true`
- **THEN** the enrolment panel runs with the token as Bearer and, after the recovery codes are acknowledged, the user returns to the password step

### Requirement: Enrolment panel

The enrolment panel SHALL begin enrolment, show the secret and the `otpauth://` URI, confirm a 6-digit code and show the recovery codes exactly once.

#### Scenario: Loading and begin error states

- **WHEN** enrolment is being started or fails
- **THEN** a loading indicator, or an error with a retry action, is shown (409 reads as already enrolled, 429 as locked)

#### Scenario: Confirm shows recovery codes once

- **WHEN** a valid code is confirmed
- **THEN** the recovery codes returned by the call are listed and a done action is available only after the user acknowledges having saved them

#### Scenario: Wrong confirmation code

- **WHEN** confirm answers 400
- **THEN** an error is shown and the form stays editable

### Requirement: Security settings page

The signed-in user SHALL manage their second factor at `/dashboard/security`.

#### Scenario: Enrol from the dashboard

- **WHEN** the signed-in user starts enrolment on the page
- **THEN** the panel runs on the session cookie without a Bearer token

#### Scenario: Disable requires a current code

- **WHEN** the user submits a TOTP or recovery code to disable
- **THEN** it is posted to `/mfa/disable` and, on 204, the page reports MFA as disabled; a 400 shows an error
