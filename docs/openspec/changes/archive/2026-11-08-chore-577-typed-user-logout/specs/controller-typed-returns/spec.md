# controller-typed-returns Delta

## MODIFIED Requirements

### Requirement: User login handler is typed

`UserController#login` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Lockout and rejected login SHALL be mapped by `UserLoginExceptionHandler` with the same HTTP shapes clients already receive: a 429 body `{"valid": false, "message": …}` and a 200 body `{"valid": false}`. Success stays 200 with `valid: true`, `token`, user fields, and `person` only when the user has a person id.

#### Scenario: login leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `UserController#login` is absent from the baseline and is not loosely typed

#### Scenario: Rejected and locked logins keep their HTTP bodies

- **GIVEN** a login call that is locked, rejected (not found, bad credentials, or inactive), or fails unexpectedly
- **WHEN** the controller advice handles the domain exception
- **THEN** lockout responds 429 with `valid` false and `message`, and rejection responds 200 with only `valid` false

## ADDED Requirements

### Requirement: User logout handler is typed

`UserController#logout` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>`, `ResponseEntity<?>`, or `ResponseEntity<Map<String, Object>>`. A presented token, a missing token, and an invalid token stay 200 with `{"ok": true}` and the cleared session cookie.

#### Scenario: logout leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `UserController#logout` is absent from the baseline and is not loosely typed

#### Scenario: Logout keeps the success body and cleared cookie

- **GIVEN** a logout call with a bearer token, or with no token
- **WHEN** the controller handles it
- **THEN** the response is 200 with `ok` true and a Set-Cookie that clears the session cookie
- **AND** revocation receives the presented token, or null when none is presented
