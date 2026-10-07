# api-ui-reachability — delta

## Purpose

Every backend REST endpoint is reachable from the UI or explicitly allowlisted.

## MODIFIED Requirements

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
