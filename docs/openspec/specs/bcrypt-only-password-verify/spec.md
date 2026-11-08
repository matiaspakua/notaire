# bcrypt-only-password-verify Specification

## Purpose
Eliminate Sonar `java:S4790` weak hashing from the login path by verifying
passwords with BCrypt only after migrating the known V2 admin seed (CU78 / RNF-14).

## Requirements

### Requirement: Login verifies BCrypt hashes only

`POST /api/v1/users/login` MUST accept a password only when the stored hash is a
BCrypt value (`$2…`) and `PasswordEncoder.matches` succeeds. The API MUST NOT
compute or compare MD5 digests for verification.

#### Scenario: BCrypt login succeeds

- **WHEN** an active user stores a BCrypt hash of the submitted password
- **THEN** login returns `valid: true` and does not rewrite the stored hash

#### Scenario: Legacy MD5 login is rejected

- **WHEN** an active user still stores the historical MD5 hex of the password
- **THEN** login returns `valid: false` and the stored hash is unchanged

### Requirement: Seed admin password is BCrypt after V52

Existing installs that still hold the V2 seed `MD5("admin")` MUST receive a
Flyway migration that replaces that exact hash with a BCrypt cost-12 hash of
`admin`. Fresh test seeds MUST insert BCrypt, not MD5.

#### Scenario: V52 replaces the known MD5 seed

- **WHEN** Flyway applies V52 against a database whose admin password is still
  `21232f297a57a5a743894a0e4a801fc3`
- **THEN** that row stores a `$2a$12$…` BCrypt hash of `admin`
