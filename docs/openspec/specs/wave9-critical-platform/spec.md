# wave9-critical-platform Specification

## Purpose
TBD - created by archiving change chore-1963-wave9-top10-critical. Update Purpose after archive.
## Requirements
### Requirement: Role module grants are enforced on the API

The system SHALL deny authenticated requests that require a module not granted
to the caller's role in `role_modules` (ADR-029 / #1683).

#### Scenario: Granted module allows access

- **WHEN** a user whose role includes module `payments` calls a payments API
- **THEN** authorization succeeds subject to existing role checks

#### Scenario: Missing module is denied

- **WHEN** a user whose role lacks module `payments` calls a payments API
- **THEN** the API responds 403

### Requirement: Invalid input is 400; missing resource is 404

Budget, Management and Payment controllers SHALL map validation-style
`IllegalArgumentException` to 400 and missing-resource cases to 404 (#1921).

#### Scenario: Invalid payment update value

- **WHEN** a payment update fails with an invalid-argument message that is not not-found
- **THEN** the response status is 400

#### Scenario: Missing payment

- **WHEN** a payment id does not exist
- **THEN** the response status is 404

### Requirement: Accepted breaking-changes list stays empty on main

On `main`, `accepted-breaking-changes.d` SHALL contain no active `*.txt` ignore
entries; the guard script SHALL fail stale entries (#1920 / #1315).

#### Scenario: Empty effective ignore list

- **WHEN** the accepted-breaking checker runs against main with only the README
- **THEN** the effective ignore list used by oasdiff is empty

### Requirement: Unbounded collections are not EAGER

Entity `@OneToMany` collections that previously used `FetchType.EAGER` SHALL use
`FetchType.LAZY` (#595).

#### Scenario: Concept items collection is LAZY

- **WHEN** ArchUnit or a unit assertion inspects `Concept` items mapping
- **THEN** fetch type is LAZY

### Requirement: Isolation checklist is verifiable

A checklist/script SHALL fail when JWT secret, admin password, or actuator
credentials are missing or equal to documented defaults (#1684).

#### Scenario: Default JWT secret fails the checklist

- **WHEN** the isolation checker runs with a default JWT secret
- **THEN** it exits non-zero

### Requirement: CI scanners fail the build

Trivy SHALL fail the job on CRITICAL findings; Checkstyle and SpotBugs SHALL
fail when findings exceed the recorded ratchet baseline (#566).

#### Scenario: Trivy CRITICAL fails CI

- **WHEN** Trivy reports one or more CRITICAL vulnerabilities in the gated scan
- **THEN** the CI job fails

### Requirement: HTTPS profile exists for local prove-out

The backend SHALL expose a documented HTTPS/TLS configuration path with
self-signed local instructions and a production certificate runbook (#254).

#### Scenario: HTTPS profile loads SSL properties

- **WHEN** the `https` (or equivalent) Spring profile is active with keystore config
- **THEN** `server.ssl` properties are present in the effective configuration

### Requirement: Remaining write controllers use Bean Validation

Write endpoints that still accept unvalidated bodies SHALL use `@Valid` (or
equivalent) and return 400 on constraint violations (#655).

#### Scenario: Empty create body on a previously unvalidated controller

- **WHEN** a client POSTs `{}` to a controller brought under validation in this wave
- **THEN** the response is 400

