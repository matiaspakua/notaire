# Local Grafana and SonarQube open without a login

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1413 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `cursor/1413-local-tools-no-login-69d3` |
| Gate 1 status | passed |

## Objetivo

Local operators must type Grafana and SonarQube credentials every time they open
overview dashboards, which blocks the at-a-glance workflow from #1408. The local
observability compose should match Prometheus/Loki/Homer: open without a login,
while production overlays keep authentication.

## What Changes

- Enable Grafana anonymous access with Admin org role and hide the login form in
  `infra/observability/grafana/grafana.ini`.
- Set SonarQube `sonar.forceAuthentication=false` for the local compose service
  so projects can be browsed anonymously (analysis still uses a token).
- Bind Grafana (`3001`) and SonarQube (`9000`) host ports to `127.0.0.1`.
- Add `infra/tests/test_local_tools_no_login.py`; leave prod compose and
  Kustomize unchanged.
- Update ADR-016, infra configuration/operation docs. CHANGELOG deferred while
  #1518 holds the hot-file lock.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Local Grafana opens without a login as Admin | CU77 | New |
| Local SonarQube allows anonymous browse | CU77 | New |
| Anonymous local tools are loopback-only | CU77 | New |
| Production authentication stays required | CU77 / ADR-016 | Made explicit |

## Capabilities

### New Capabilities

- `local-tools-no-login`: local Grafana/SonarQube usable without credentials,
  bound to localhost.

### Modified Capabilities

None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | No | |
| `infra` | Yes | grafana.ini, observability compose ports/env, tests, docs, ADR-016 |
| CI/CD | No | |

### Surface area

- No product endpoints, Flyway, or `.env` keys.
- Local compose only; `docker-compose.prod.yml` and `infra/deploy/kustomize` unchanged.

### Architecture review

Amends ADR-016 with the local-only no-login decision. No new ADR file.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-016-observability-stack.md` | Local anonymous + loopback binding |
| `infra/docs/CONFIGURATION.md` | Login column for Grafana/Sonar |
| `infra/docs/OPERATION.md` / `infra/scripts/start-infra.sh` | No-login local URLs |
| `infra/MODULE.md` | Brief invariant |
| `CHANGELOG.md` | Deferred (#1518 lock) |

## Out of Scope

- Homer coverage (#1468, already merged).
- Client error beacon (#1483).
- Exposing Grafana/Sonar on the LAN or in prod without auth.
