# OpenTelemetry collector, Notaire metrics and exporters in Prometheus

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1462 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1462_otel_metrics_exporters` |
| Gate 1 status | Pending |

## Objetivo

Prometheus today sees the backend (Actuator scrape), PostgreSQL and the observability tools.
It sees nothing of the frontend, the containers or the host, and nothing flows through OpenTelemetry.
Make every major module produce metrics and route them into Prometheus.

## What Changes

- Add an OpenTelemetry Collector (OTLP in, Prometheus exporter out) to the observability stack.
- Backend exports its Micrometer metrics over OTLP to the collector, **in addition to** the Actuator scrape.
- Frontend exposes Prometheus metrics (Node runtime + HTTP request counters) at `/api/metrics`.
- Add node-exporter (host/VM) and cAdvisor (containers); Prometheus scrapes both, the collector,
  and the frontend, with stable `service` labels.
- Guard tests, and documentation of the metrics topology.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must see infrastructure and application metrics through Prometheus. | CU77 | Made explicit |
| Telemetry export must not break the application when the collector is down. | CU77 availability objective | New |

## Capabilities

### New Capabilities
- `otel-metrics-pipeline`: metrics from backend, frontend, containers and host reach Prometheus.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | Yes | `micrometer-registry-otlp` dependency and OTLP export properties; no Java code. |
| `frontend` | Yes | `prom-client` dependency and one route handler `src/app/api/metrics/route.ts`. |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Collector, node-exporter, cAdvisor, Prometheus scrape jobs, tests, docs. |
| CI/CD | No | Existing gates run the new tests. |

### Surface area

- Endpoints: new `GET /api/metrics` on the frontend (operational, unauthenticated, bound to the Docker network only; not published through the BFF proxy). No `/api/v1` change, no OpenAPI impact.
- Entities / Flyway: none. `.env`: none new.
- Dependencies: `micrometer-registry-otlp` (Spring-managed), `prom-client`, pinned images
  `otel/opentelemetry-collector-contrib`, `prom/node-exporter`, `gcr.io/cadvisor/cadvisor`.
- Compatibility: additive.

### Architecture review

Extends ADR-016; the collector is the single OTLP ingress that Tempo (#1464) will reuse. No new ADR;
ADR-016's inventory is updated.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/202-ADR/ADR-016-observability-stack.md` | Inventory: collector, node-exporter, cAdvisor. |
| `infra/docs/DEFINITION.md` | Scrape targets table. |
| `docs/200-architecture/207-monitoring/README.md` | Metrics topology. |
| `CHANGELOG.md` | N/A: operational tooling. |

## Out of Scope

- Traces (#1464), logs (#1463), alert rules (#1465), new dashboards (#1467).
