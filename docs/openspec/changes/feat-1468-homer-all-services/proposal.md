# Homer lists every service and dashboard

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1468 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1468_homer_all_services` |
| Gate 1 status | Pending |

## Objetivo

Homer is the landing page of the stack, but it omits Alertmanager and the exporters, lists only some dashboards, and nothing checks that it stays in sync with the compose files. Make it show every service and dashboard, and guard it.

## What Changes

- Add entries for Alertmanager, Tempo, OTel Collector, blackbox, node-exporter, cAdvisor, Promtail and postgres-exporter; services without a browsable port link to the Grafana view that shows their health.
- Add every provisioned Grafana dashboard, grouped (overviews, drill-down, specialists, security) and the SonarQube backend and frontend project links.
- A guard test compares compose services to Homer entries and provisioned dashboard UIDs to Homer links, so a new service or dashboard cannot be forgotten.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The landing page lists every operational service and dashboard. | CU77 | Made explicit |

## Capabilities

### New Capabilities
- `homer-hub`: Homer reflects the compose services and Grafana dashboards.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | No | |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Homer `config.yml`, guard test, docs. |
| CI/CD | No | |

### Surface area

- Endpoints/entities/Flyway/`.env`: none. Compatibility: additive.

### Architecture review

No ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/docs/DEFINITION.md` | Homer entry mentions the guard and groups. |
| `CHANGELOG.md` | N/A: local tooling. |

## Out of Scope

- Server-side health checks (Homer tests from the browser); changing Homer's image or theme.
