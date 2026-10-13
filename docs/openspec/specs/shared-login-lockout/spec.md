# shared-login-lockout Specification

## Purpose
TBD - created by archiving change feat-1634-shared-login-lockout. Update Purpose after archive.

## Requirements

### Requirement: Login lockout state is shared and durable

Failed-login counters and lock state MUST be stored in the database keyed by the normalised username, so a lock set through one `LoginAttemptService` instance applies to every instance and survives a restart. The threshold and lock duration MUST stay those of `security.login.*`. Expired locks and idle counters older than the lock duration MUST be purged, and no data other than the normalised username, counter and timestamps MUST be stored.

#### Scenario: a lock applies across instances

- **WHEN** two service instances share one store and the threshold is reached through the first
- **THEN** the second reports the username as locked

#### Scenario: a restart does not reset the lock

- **WHEN** a new service instance is created over the same store after a lock
- **THEN** the username is still locked

#### Scenario: a success clears the counter on every instance

- **WHEN** a login succeeds through one instance after some failures
- **THEN** the counter restarts from zero on the other instance

#### Scenario: expired entries are purged

- **WHEN** a failure is recorded after the lock duration has elapsed for other users
- **THEN** their idle or expired rows are removed

#### Scenario: the table holds no personal data beyond the username

- **WHEN** the columns of `login_attempts` are listed
- **THEN** they are username, failed count, locked until and updated at
