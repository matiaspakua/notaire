# backend-logging Specification

## Purpose
Backend logs are shipped to the logging platform and never kept in the codebase. Source: #1286; owner CU77.

## Requirements

### Requirement: The backend emits logs only as structured JSON on the console

The backend MUST NOT write log files, and every root log event MUST go to the JSON console appender that Promtail scrapes.

#### Scenario: No file appender is configured

- **WHEN** the Logback configuration is loaded
- **THEN** it declares no file appender and the root logger references only the JSON console appender

#### Scenario: No log file location is configured

- **WHEN** `application.properties` is read
- **THEN** it defines no `logging.file.*` key

#### Scenario: Log directory is not tracked

- **WHEN** a developer runs the backend from `backend-api/`
- **THEN** `backend-api/logs/` does not appear in `git status`

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
