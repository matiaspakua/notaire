# Infrastructure, frontend, pipeline-health and trace dashboards

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1467 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1467_infra_dashboards` |
| Gate 1 status | Pending |

## Objetivo

The two overview dashboards (#1408) show availability. Now that the stack produces host, frontend, trace and alert
telemetry, operators need drill-down views for each, and the observability tools themselves must be monitored.

## What Changes

- `notaire-infra-resources`: Docker host CPU, memory, load, disk, network and aggregate container usage (node-exporter, cAdvisor).
- `notaire-frontend`: Node runtime (heap, event loop lag, GC, CPU, handles) and proxied-request rate/status/errors from logs.
- `notaire-observability-health`: Prometheus, Loki, Tempo, Alertmanager, Promtail and the collector (series, ingest, failures).
- `notaire-traces`: recent traces (Tempo) with request logs and latency from the structured logs.
- Add Prometheus scrape jobs for Tempo, Alertmanager and Promtail, and enable the collector's own metrics.
- Link every dashboard to the others; guard tests; the existing backend, PostgreSQL, auth and logs dashboards are kept.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| The monitoring tools must themselves be observable. | CU77 | New |
| A dashboard panel must not fail when its metric is absent. | CU77 | Made explicit (as #1408) |

## Capabilities

### New Capabilities
- `infra-dashboards`: drill-down dashboards for host, frontend, observability health and traces.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | |
| `frontend` | No | |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Dashboards, scrape jobs, collector telemetry, tests, docs. |
| CI/CD | No | Existing gates. |

### Surface area

- Endpoints / entities / Flyway / `.env`: none. No new images.
- Compatibility: additive; existing dashboard UIDs unchanged.

### Architecture review

Follows ADR-016 and the provisioned-dashboard pattern from #1408. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/MODULE.md`, `infra/docs/DEFINITION.md`, `infra/docs/OPERATION.md` | Dashboard catalogue and scrape targets. |
| `docs/200-architecture/207-monitoring/README.md` | Dashboard map. |
| `CHANGELOG.md` | N/A: operational. |

## Out of Scope

- Per-container views (cAdvisor on Docker Desktop reports cgroup ids only); Homer links (#1468); a new PostgreSQL dashboard (the existing one stays).
