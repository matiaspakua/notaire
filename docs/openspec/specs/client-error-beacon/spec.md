# client-error-beacon Specification

## Purpose
Browser client errors reach Loki through a safe, validated frontend beacon so
CU77 operators can inspect them with `service=notaire-frontend`.
## Requirements
### Requirement: Beacon accepts validated client errors

The frontend SHALL expose `POST /api/client-errors` that accepts a JSON body with
allow-listed fields only: required `level` (`ERROR` or `WARN`) and `message`, and
optional `name`, `path` (pathname without query), and `stack`. On success the
handler SHALL respond with HTTP 204 and SHALL emit one structured log line with
`service` equal to `notaire-frontend` and a message that identifies a client error.

#### Scenario: Valid client error is logged

- **WHEN** a client POSTs a valid JSON body with `level` ERROR and a non-empty `message`
- **THEN** the response status is 204 and the server stdout contains a JSON log
  entry with `service` `notaire-frontend` and a `client_error` message prefix

### Requirement: Beacon rejects malformed and oversized input

The beacon SHALL reject missing or invalid `level`/`message`, unknown JSON keys,
and payloads that exceed configured size caps, without writing an accepted
client-error log line.

#### Scenario: Malformed or incomplete body is rejected

- **WHEN** a client POSTs invalid JSON, omits `message`, or sends an unsupported `level`
- **THEN** the response status is 400 and no accepted `client_error` log line is written

#### Scenario: Oversized body is rejected

- **WHEN** a client POSTs a body larger than the configured maximum raw size
- **THEN** the response status is 413 and no accepted `client_error` log line is written

#### Scenario: Unknown fields are rejected

- **WHEN** a client POSTs a JSON object that includes keys outside the allow-list
- **THEN** the response status is 400 and no accepted `client_error` log line is written

### Requirement: Beacon rate-limits per client

The beacon SHALL rate-limit accepted write attempts per client identity (derived
from request headers) and SHALL respond with HTTP 429 when the limit is exceeded.

#### Scenario: Excess requests from one client are rate-limited

- **WHEN** a single client identity exceeds the configured request budget in the window
- **THEN** further POSTs from that identity receive status 429

### Requirement: Beacon never logs sensitive request material

Accepted log lines SHALL NOT include cookies, Authorization headers, or URL query
strings from the inbound request.

#### Scenario: Sensitive material is absent from the log line

- **WHEN** a valid beacon POST includes Cookie or Authorization headers and a
  `path` without a query string
- **THEN** the structured log JSON does not contain cookie values, Authorization
  values, or a `?` query fragment

