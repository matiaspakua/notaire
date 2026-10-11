# Validate that every infra service did its job after an E2E run

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1469 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1469_infra_e2e_validation` |
| Gate 1 status | Pending |

## Objetivo

The observability stack is configured but nothing proves it works end to end. Add one command that checks, after the Playwright E2E suite has generated traffic, that metrics, logs, traces, alerts and the quality portal all hold real data.

## What Changes

- `infra/scripts/verify_signals.py`: queries Prometheus, Loki, Tempo (via Grafana), Alertmanager and SonarQube and prints PASS/FAIL/SKIP for each signal; exits non-zero if any signal fails.
- `infra/scripts/verify-signals.sh`: optional `--with-e2e` runs the Playwright suite first (`testing/scripts/run.sh e2e`), then the checks.
- Unit tests for the check logic with a stubbed HTTP layer; docs for the command.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must be able to trust that telemetry flows. | CU77 | New |

## Capabilities

### New Capabilities
- `infra-signal-validation`: scripted end-to-end check of the observability signals.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | No | |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | New script, tests, docs. |
| `testing` | No | Reuses `testing/scripts/run.sh e2e`. |

### Surface area

- Endpoints/entities/Flyway: none. `.env`: reads existing `GRAFANA_ADMIN_*` and `SONAR_ADMIN_*` with defaults; no new keys.
- Compatibility: additive.

### Architecture review

No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/docs/OPERATION.md` | How to run the validation and read its output. |
| `CHANGELOG.md` | N/A: local tooling. |

## Out of Scope

- Running it in CI (needs the full observability stack); asserting dashboard rendering in the browser.
