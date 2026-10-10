## Purpose

Let operators move from a request's log line to its trace, in both modules.

## ADDED Requirements

### Requirement: Backend request log

The backend SHALL write one structured INFO line per API request containing method, path, status and duration,
inside the trace scope so `traceId` and `spanId` are present. It MUST NOT log the query string, headers or body,
and MUST skip Actuator paths.

#### Scenario: API request is logged with its trace id

- **WHEN** the backend serves an API request
- **THEN** one `http_request` log line is written with method, path, status and duration, carrying the active `traceId`

#### Scenario: Sensitive and noisy data is excluded

- **WHEN** a request has a query string or targets an Actuator path
- **THEN** the query string is absent from the log line, and Actuator requests produce no line

### Requirement: Frontend proxy log

The frontend BFF proxy SHALL log each proxied request and each upstream failure as structured JSON with
`service`, `level`, method, path and status, and MUST NOT log cookies, headers or query strings.

#### Scenario: Proxied request is logged

- **WHEN** the proxy forwards a request
- **THEN** one structured log line is emitted with method, path and upstream status

#### Scenario: Upstream failure is logged as an error

- **WHEN** the upstream is unreachable
- **THEN** an ERROR structured line is emitted and the client receives 502

### Requirement: Log to trace navigation

Grafana SHALL provision a Loki derived field that links `traceId` in a log line to the Tempo datasource.

#### Scenario: Derived field is provisioned

- **WHEN** Grafana starts
- **THEN** the Loki datasource defines a `traceId` derived field pointing at datasource `tempo`
