<!-- Governed by CONSTITUTION.md. Issue #2351. The issue stays open. -->

## ADDED Requirements

### Requirement: Login-attempt entity is not in the shared business package

The failed-login counter entity SHALL live with the login-attempt persistence code and SHALL keep the table name `login_attempts`.

#### Scenario: the shared business package does not contain the entity

- **WHEN** the login-attempt type is loaded
- **THEN** it is `adapter.out.persistence.security.LoginAttempt`, it is a JPA entity, its table is `login_attempts`, and `business.LoginAttempt` does not exist
