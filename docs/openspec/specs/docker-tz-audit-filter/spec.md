# docker-tz-audit-filter Specification

## Purpose
TBD - created by archiving change fix-2013-docker-tz-audit-filter. Update Purpose after archive.
## Requirements
### Requirement: Compose stack inherits operator TZ

The application Compose services SHALL receive a `TZ` environment variable so the JVM default zone used by CU23 audit day filters matches the calendar the browser/Playwright uses for `from`/`to`.

#### Scenario: Host zone exported into backend

- **GIVEN** the operator host uses IANA zone `Europe/Madrid` and `TZ` is unset in the shell
- **WHEN** `workspace/stack/start.sh` starts the stack
- **THEN** the backend container's `date` reflects that zone (not bare UTC) and CU23 filtering for `today` includes rows just written from the UI

