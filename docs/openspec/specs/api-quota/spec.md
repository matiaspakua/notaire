# api-quota Specification

## Purpose

Define the API rate-limiting policy: per-caller request counting in a shared database window, 429 rejection behaviour, safe concurrent window creation, and operational defaults for e2e versus production stacks.

## Requirements

### Requirement: API calls have a per-caller quota

The API SHALL count requests to `/api/**` in a fixed window stored in `api_rate_windows`, shared by every API instance. The key SHALL be the authenticated username when a JWT has already been accepted, and the servlet remote address otherwise. When the count for that key exceeds the configured maximum, the API SHALL respond with HTTP 429 and a `Retry-After` header, and SHALL NOT invoke the rest of the chain. Login username lockout SHALL stay a separate control.

#### Scenario: A burst is rejected and a later window is allowed

- **WHEN** a caller exceeds the configured maximum inside one window and then calls again after that window
- **THEN** the excess call is rejected and the later call is allowed

#### Scenario: Another instance sees the same count

- **WHEN** one API instance has already counted a caller up to the maximum
- **THEN** another instance using the same table rejects the next call from that caller

### Requirement: 429 responses are logged without personal data

The system SHALL log every HTTP 429 (Too Many Requests) at WARN level with the key type (`user` or `ip`) and the request URI path, and SHALL NOT include the username or the IP address in the log line.

#### Scenario: 429 logged with key type and path for a user key

- **WHEN** an authenticated user exceeds the rate limit and receives HTTP 429
- **THEN** a WARN log line is emitted containing the key type `user` and the request URI, and the log line does not contain the username

#### Scenario: 429 logged with key type and path for an IP key

- **WHEN** an unauthenticated caller (identified by IP) exceeds the rate limit and receives HTTP 429
- **THEN** a WARN log line is emitted containing the key type `ip` and the request URI, and the log line does not contain the IP address

### Requirement: Concurrent first requests are all counted

The system SHALL count every concurrent first request in a new rate-limit window without throwing an exception, even when multiple threads attempt to create the window row simultaneously.

#### Scenario: Concurrent first requests all counted

- **WHEN** 32 concurrent requests arrive for the same key in a new window
- **THEN** all 32 are counted (the window's `request_count` equals 32) and no exception is thrown

### Requirement: E2e stack uses a higher rate limit; production stays at 2000

The e2e/preflight stack SHALL start the backend with `NOTAIRE_API_RATE_LIMIT_MAX` set to at least 20 000 so that a full Playwright run never hits the ceiling. The production default in `application.properties` SHALL remain 2000, and `docker-compose.prod.yml` SHALL NOT set a value above 2000.

#### Scenario: E2e stack limit is 20000, production default is 2000

- **WHEN** the e2e stack is started through `preflight.sh` (which calls `workspace/stack/start.sh --no-admin`)
- **THEN** the backend container receives `NOTAIRE_API_RATE_LIMIT_MAX=20000`, while `application.properties` default remains 2000 and `docker-compose.prod.yml` does not override it above 2000
