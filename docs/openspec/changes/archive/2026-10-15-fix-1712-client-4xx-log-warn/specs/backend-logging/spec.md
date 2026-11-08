# backend-logging — delta

## Purpose

Backend structured logs use severity that matches whether the failure is the client's fault or the server's.

## ADDED Requirements

### Requirement: Client 4xx failures log as WARN

When the API answers an HTTP status below 500 for an application exception (`NotaireException` or a controller catch that returns 4xx), the backend SHALL emit a WARN structured log, not ERROR. Server faults (status 500 and above) SHALL still log ERROR.

#### Scenario: Conflict NotaireException

- **WHEN** a `NotaireException` with status 409 is handled (for example duplicate or in-use identification type)
- **THEN** the structured log level is WARN and the HTTP response remains 409

#### Scenario: Server NotaireException

- **WHEN** a `NotaireException` with status 500 is handled
- **THEN** the structured log level is ERROR

#### Scenario: Delete conflict

- **WHEN** deleting a management fails on a foreign-key constraint and the controller answers 409
- **THEN** the controller log level is WARN, not ERROR
