# Run the k6 load test on the runner so it can reach the backend

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1266 |
| Use Case | CU74 – Performance and Caching Strategy |
| Branch | `ci/1266_k6_runner_network` |
| Gate 1 status | draft |

## Objetivo

The weekly k6 load test never exercises the API: the container action `grafana/k6-action` cannot reach the backend that listens on the runner's `localhost:8080`, so `setup()` fails with connection refused. Run k6 on the runner.

## What Changes

- `.github/workflows/performance-test.yml` installs k6 with `grafana/setup-k6-action` and runs `k6 run infra/performance/k6/load-test.js`; the `summary.json` upload stays.
- `scripts/test_performance_test_assets.py` fails if the workflow uses the container action.
- No production code, test data or configuration change.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The weekly load test must exercise the running backend | CU74, #1266 | Made explicit |

## Capabilities

### New Capabilities

- (none — `skip_specs: true`)

### Modified Capabilities

- (none)

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | no | — |
| `frontend` | no | — |
| `notaire-shared` | no | — |
| CI/CD (`.github/workflows`) | yes | one workflow step |

### Surface area

- Entities / Endpoints / Flyway / Configuration: none
- Dependencies: none

### Architecture review

CI wiring only. No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/300-development/CI-PREFLIGHT.md` | n/a (no local gate changes) |
| `CHANGELOG.md` | n/a - not user visible |
