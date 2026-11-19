# api-quota Specification

## Purpose
TBD - created by archiving change fix-2010-api-quota. Update Purpose after archive.

## Requirements

### Requirement: API calls have a per-caller quota

The API SHALL count requests to `/api/**` in a fixed window. The key SHALL be the authenticated username when a JWT has already been accepted, and the servlet remote address otherwise. When the count for that key exceeds the configured maximum, the API SHALL respond with HTTP 429 and a `Retry-After` header, and SHALL NOT invoke the rest of the chain. Login username lockout SHALL stay a separate control.

The counters SHALL live in the API process. A deployment with more than one API instance does not share them. That limit is recorded on SR-15, and issue #2010 stays open until a shared store exists.

#### Scenario: A burst is rejected and a later window is allowed

- **WHEN** a caller exceeds the configured maximum inside one window and then calls again after that window
- **THEN** the excess call is rejected and the later call is allowed
