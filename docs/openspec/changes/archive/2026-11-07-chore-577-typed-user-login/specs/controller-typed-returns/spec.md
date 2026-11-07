# controller-typed-returns Delta

## ADDED Requirements

### Requirement: User login handler is typed

`UserController#login` SHALL NOT appear in `controller-loose-returns.txt`. Its declared return type SHALL NOT be `ResponseEntity<Object>` or `ResponseEntity<?>`. Lockout and rejected login SHALL be mapped by `UserLoginExceptionHandler` with the same HTTP shapes clients already receive: a 429 body `{"valid": false, "message": …}` and a 200 body `{"valid": false}`. Success stays 200 with `valid: true`, `token`, user fields, and `person` only when the user has a person id. `UserController#logout` stays on the baseline.

#### Scenario: login leaves the loose baseline

- **GIVEN** the controller signature architecture test and baseline file
- **WHEN** the suite runs after this change
- **THEN** `UserController#login` is absent from the baseline and is not loosely typed
- **AND** `UserController#logout` remains on the baseline

#### Scenario: Rejected and locked logins keep their HTTP bodies

- **GIVEN** a login call that is locked, rejected (not found, bad credentials, or inactive), or fails unexpectedly
- **WHEN** the controller advice handles the domain exception
- **THEN** lockout responds 429 with `valid` false and `message`, and rejection responds 200 with only `valid` false
