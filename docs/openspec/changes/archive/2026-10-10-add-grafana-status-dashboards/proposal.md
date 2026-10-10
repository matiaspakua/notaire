# Add Grafana technology-stack and Notaire status dashboards

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1408 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1408_grafana_status_dashboards` |
| Gate 1 status | Pending |

## Objetivo

Operations staff currently have to correlate several Grafana dashboards and service
UIs to establish whether Notaire is usable. Add two concise, provisioned overview
dashboards so the first response to an incident is based on one consistent view.

## What Changes

- Add a technology-stack dashboard for Prometheus target availability, active
  alerts, observability-service status, and recent cross-stack errors from Loki.
- Add a Notaire status dashboard for frontend, backend API, PostgreSQL, API
  availability, and active Notaire alerts.
- Add lightweight Prometheus HTTP/TCP probes for services that currently expose
  no scrapeable availability metric, so all status shown by the dashboards is
  an observed signal rather than an inference from logs.
- Mount `alert-rules.yml` into Prometheus (it was never loaded, so no alert was ever
  evaluated) and add a `ServiceDown` alert on the probes, so the alert panels are real.
- Add dashboard-schema/query guard tests and document the new overview-to-drill-down
  navigation alongside the existing specialist dashboards.
- Preserve `notaire-backend`, `notaire-postgres`, `notaire-auth`, and
  `notaire-logs` as detailed diagnostic views; no dashboard is removed.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must be able to assess operational availability and investigate incidents through Prometheus, Grafana, and Loki. | `docs/100-business/102-use-cases/CU77 – Operations Monitoring and Incident Management.md` | Made explicit |
| An overview must show status and route diagnosis to focused telemetry rather than duplicate detailed metrics. | CU77; existing dashboard contract in `infra/MODULE.md` | New |
| Absent optional telemetry must be visible as no data/unavailable and must not make a dashboard query fail. | CU77 availability objective | New |

## Capabilities

### New Capabilities
- `observability-status-dashboards`: Provisioned Grafana overview dashboards
  for stack and Notaire service status, including their operational behavior.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | No | Existing Actuator metrics remain the API availability signal. |
| `frontend` | No | The dashboard uses existing frontend container logs; it does not change the product UI. |
| `frontend-swing` | No | Retired; out of scope. |
| `infra` / observability | Yes | Add HTTP/TCP availability probes, provisioned Grafana JSON, guard tests, and operations documentation. |
| CI/CD (`.github/workflows`) | No | Existing verification runs the new infra guard tests without workflow changes. |

### Surface area

- Entities: None.
- Endpoints: No product endpoint changes. Existing service health endpoints,
  `/actuator/prometheus`, Grafana `/metrics`, Loki `/metrics`, and Prometheus
  alert data are the sources; the probe exporter supplies an internal metrics endpoint.
- Database (Flyway `V{n}`): No schema change or Flyway migration.
- Configuration / `.env`: No new keys and no credentials in dashboard definitions.
- Dependencies: Existing provisioned Prometheus and Loki data sources plus a
  pinned blackbox-exporter container in the existing observability Compose stack.
- Compatibility: No breaking API, database, or client change.

### Architecture review

The change follows the provisioned-dashboard and Prometheus scrape patterns and
the separate-stack topology in ADR-016. The probe exporter is an implementation
addition to that accepted topology, not a new architecture decision; ADR-016
must be updated to inventory the new component, but a separate ADR is not required.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `infra/MODULE.md` | Add both overview dashboards to the module contract while retaining specialist dashboards. |
| `infra/docs/DEFINITION.md` | Document the dashboard purpose, scope, and metric/log sources. |
| `infra/docs/OPERATION.md` | Add the dashboard selection and incident-triage workflow. |
| `docs/200-architecture/207-monitoring/README.md` | List the overview dashboards and their relationship to existing drill-down dashboards. |
| `docs/200-architecture/202-ADR/ADR-016-observability-stack.md` | Add the health-probe component to the accepted stack inventory. |
| `CHANGELOG.md` | N/A: local operational-dashboard provisioning, not a released end-user change. |

## Out of Scope

- Changing application instrumentation or alert-routing; ADR-016 records that
  Alertmanager is not deployed and this change keeps that decision.
- Replacing the existing backend, PostgreSQL, authentication, or logs dashboards.
- Adding commercial/managed observability services or production notification channels.
