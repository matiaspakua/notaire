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

