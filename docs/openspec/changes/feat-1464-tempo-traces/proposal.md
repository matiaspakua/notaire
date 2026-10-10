# Tempo for distributed traces

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1464 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1464_tempo_traces` |
| Gate 1 status | Pending |

## Objetivo

The backend creates trace ids (Brave) but exports them to a Zipkin endpoint that does not exist, so traces
are lost. Add Grafana Tempo as the trace store, fed through the OTel Collector, and make it explorable in Grafana.

## What Changes

- Add Tempo (pinned) with local storage; add a `traces` pipeline in the collector forwarding OTLP to Tempo.
- Backend: replace the Brave tracer and Zipkin reporter with the OpenTelemetry tracer and OTLP trace export to the collector.
- Grafana: provisioned Tempo datasource (uid `tempo`), with trace-to-logs link to Loki and Tempo-to-Prometheus service map.
- Guard tests and documentation.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must be able to follow a request across modules. | CU77 | Made explicit |
| Trace export failure must not affect requests. | CU77 availability objective | New |

## Capabilities

### New Capabilities
- `distributed-tracing`: request traces from the backend stored in Tempo and explorable in Grafana.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | Yes | Dependencies (brave/zipkin replaced by OTel bridge + OTLP exporter) and properties; no Java code. |
| `frontend` | No | Frontend trace propagation is out of scope. |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Tempo service/config, collector pipeline, Grafana datasource, tests, docs. |
| CI/CD | No | Existing gates. |

### Surface area

- Entities / endpoints / Flyway / `.env`: none.
- Dependencies: `spring-boot-starter-opentelemetry` replaces `micrometer-tracing-bridge-brave` and `zipkin-reporter-brave`; image `grafana/tempo:2.10.8`.
- Compatibility: W3C propagation (already configured) is unchanged.

### Architecture review

Extends ADR-016; Tempo is the third pillar next to Prometheus and Loki. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `ADR-016-observability-stack.md` | Inventory: Tempo. |
| `infra/docs/DEFINITION.md` | Tempo and datasource. |
| `docs/200-architecture/207-monitoring/README.md` | Tracing topology. |
| `CHANGELOG.md` | N/A: operational tooling. |

## Out of Scope

- Frontend spans, sampling policy tuning, log correlation fields (#1463).
