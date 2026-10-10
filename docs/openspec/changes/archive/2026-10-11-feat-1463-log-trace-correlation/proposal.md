# Request logs with trace correlation in Loki

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md). This proposal documents
> **only this change**; permanent documentation remains the single source of truth.

| Field | Value |
|-------|-------|
| GitHub Issue | #1463 |
| Use Case | CU77 – Operations Monitoring and Incident Management |
| Branch | `feat/1463_log_pipeline_trace_correlation` |
| Gate 1 status | Pending |

## Objetivo

Every stack container already reaches Loki, but the backend logs nothing per request (so no line carries a
trace id) and the frontend emits no request logs. Make each request leave one structured log line in both
modules, and let Grafana jump from a log line to its trace.

## What Changes

- Backend: one INFO line per API request (`http_request`: method, path, status, duration) written inside the
  trace scope, so the JSON line carries `traceId`/`spanId`. Actuator paths are skipped.
- Frontend: the BFF proxy logs each proxied request and upstream failures through the structured logger (JSON),
  replacing bare `console.error`.
- Grafana: the Loki datasource gets a derived field that turns `traceId` into a link to Tempo.
- Guard tests and documentation.

## Reglas de negocio

| Rule | Source | New / Changed / Made explicit |
|------|--------|-------------------------------|
| Operators must be able to move from a log line to its trace. | CU77 | New |
| Logs must not contain credentials, tokens or query strings. | `.claude/rules/programming.md` (Logging) | Made explicit |

## Capabilities

### New Capabilities
- `log-trace-correlation`: per-request structured logs in backend and frontend, linked to traces.

### Modified Capabilities
None.

## Impact Analysis

### Módulos afectados

| Module | Touched | What changes |
|--------|---------|--------------|
| `backend-api` | Yes | New `RequestLoggingFilter` in `observability`. |
| `frontend` | Yes | `src/lib/backend-proxy.ts` logs through `logger`. |
| `frontend-swing` | No | Retired. |
| `infra` | Yes | Loki derived field, test, docs. |
| CI/CD | No | Existing gates. |

### Surface area

- Endpoints: none new. Entities / Flyway / `.env`: none.
- Compatibility: additive log lines only. Query strings are never logged (they may carry identifiers).

### Architecture review

Extends ADR-016; reuses the existing `LogstashEncoder` MDC keys. No new ADR.

## Documentation Impact

| Permanent document | What must change |
|--------------------|------------------|
| `docs/200-architecture/207-monitoring/README.md` | Log fields and log-to-trace navigation. |
| `infra/docs/DEFINITION.md` | Loki derived field. |
| `CHANGELOG.md` | N/A: operational. |

## Out of Scope

- Client-side error beacon (#1483); frontend spans and `traceparent` forwarding; log retention tuning.
