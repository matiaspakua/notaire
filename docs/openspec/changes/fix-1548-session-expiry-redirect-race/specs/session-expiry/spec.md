# session-expiry — delta

## Purpose

Expired-session handling in the frontend.

## MODIFIED Requirements

### Requirement: Expired-session landing

When an authenticated API call returns 401, the user SHALL land on `/login?expired=1` with the expired-session message, regardless of request timing.

#### Scenario: Stale markers at the edge

- **WHEN** `/login?expired=1` arrives with the UX status cookie still set
- **THEN** the proxy serves the login page and clears the markers

#### Scenario: Guard during expiry

- **WHEN** the store is logged out while the expiry redirect is in flight
- **THEN** the dashboard guard doesn't redirect to plain `/login`

#### Scenario: Under load

- **WHEN** TS-0093 runs 30 times on 6 workers
- **THEN** every run shows the expired message and re-logs in
