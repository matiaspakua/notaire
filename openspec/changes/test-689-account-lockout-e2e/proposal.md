# Account lockout end-to-end test

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #689 |
| Use Case | CU78 – Security and Compliance (login lockout, #560) |
| Branch | `test/689_account_lockout_e2e` |
| Gate 1 status | draft |

## Objetivo

The backend locks a username after repeated failed logins (`LoginAttemptService`, backend integration test `LoginRateLimitIntegrationTest`) and the login page shows a lockout message on HTTP 429, but no Playwright test drives the real flow through the form. Add the missing end-to-end coverage on desktop and mobile widths, without locking out a real account of the shared stack.

## What Changes

- New spec `TS-0100-login-account-lockout.spec.ts`: submit wrong credentials for a unique throwaway username until the account locks, then assert the lockout message is shown, the form is still on `/login` and no session is created.
- The same flow runs at 1024x768 and 320x640.
- `E2E-TEST-MAPPING.md` registers the spec under CU78.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| A username is locked after the configured number of consecutive failed logins (default 5 for 15 minutes) | #560, CU78 | Made explicit |

## Capabilities

### New Capabilities

- `login-lockout-e2e`: The lockout is verified through the UI.

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | behaviour unchanged |
| `frontend` | no | the lockout toast already exists |
| Docs / scripts / CI | yes | new Playwright spec, E2E mapping, CHANGELOG |

### Surface area

- Endpoints, entities, Flyway, configuration, dependencies: none
- Risk: lockout state lives in backend memory; the test uses a unique username per run so no real account is affected and the 15-minute window does not leak into other specs

### Architecture review

No architectural change.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/303-testing/E2E-TEST-MAPPING.md` | register TS-0100 |
| `CHANGELOG.md` | one entry |
