# api-ui-reachability Specification

## Purpose
Every backend REST endpoint is reachable from the UI or explicitly allowlisted.

## Requirements

### Requirement: Endpoint reachability

Every endpoint in the committed OpenAPI contract SHALL be called from `frontend/src` or SHALL be listed with a reason in `contracts/api-reachability-allowlist.yaml`, and the allowlist SHALL NOT keep entries for removed or called endpoints.

#### Scenario: Unreferenced endpoint fails

- **WHEN** an OpenAPI endpoint has no frontend literal and no allowlist entry
- **THEN** the guard fails and names the endpoint

#### Scenario: Stale allowlist entry fails

- **WHEN** an allowlisted endpoint is gone from OpenAPI or is now called by the UI
- **THEN** the guard fails and asks to remove the entry

#### Scenario: Scanner binds literals to endpoints

- **WHEN** a literal is passed to an api-client helper or contains `${...}` segments
- **THEN** it covers only that HTTP method, and a wildcard covers a fixed segment only when no path parameter fits

### Requirement: A fixed path does not cover a parameterized sibling

When several OpenAPI templates fit one frontend literal, the reachability scan SHALL keep only the templates with the fewest path parameters. A call to a fixed segment such as `/payments/date` SHALL cover `GET /payments/date` and SHALL NOT cover `GET /payments/{id}`.

#### Scenario: Date route is not a call to the id route

- **WHEN** the scanner reads `apiGet('/payments/date?from=1')`
- **THEN** `GET /api/v1/payments/date` is covered and `GET /api/v1/payments/{id}` is not
