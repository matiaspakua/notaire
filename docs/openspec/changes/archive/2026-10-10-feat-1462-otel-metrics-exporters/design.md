> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). Backend metrics reach Prometheus only through a Basic-auth Actuator scrape.

## Goals / Non-Goals

**Goals:** OTLP path through a collector; frontend, container and host metrics in Prometheus.
**Non-Goals:** traces, logs, alerting, dashboards (separate slices).

## Decisions

1. **Collector with an OTLP receiver and a Prometheus exporter (port 8889).** Prometheus scrapes the collector, so
   metrics sent by any module appear without per-service scrape config. Image pinned.
2. **Backend: `micrometer-registry-otlp`, URL from `MANAGEMENT_OTLP_METRICS_EXPORT_URL`.** Actuator scrape is kept
   (backend dashboards and `BackendDown` depend on it). Rejected: the OTel Java agent (extra moving part, duplicates Micrometer).
3. **Frontend: `prom-client` in a route handler.** Rejected: `@vercel/otel` (heavier, targets Vercel). The route is under `/api/`, which the edge proxy
   already bypasses; it is not exposed through the host port mapping policy beyond what the frontend already publishes, and carries no user data.
4. **cAdvisor + node-exporter.** On Docker Desktop they report the Linux VM; documented. cAdvisor needs read-only host mounts,
   not the Docker socket.

## Riesgos / Trade-offs

- [Same series from Actuator and OTLP] -> OTLP series pass through the collector with a `source="otlp"` resource label and the
  documented dashboards keep using the Actuator series.
- [Collector down] -> OTLP export errors are logged, never thrown.
- [cAdvisor privileges] -> read-only mounts only, no `privileged`.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Collector is scraped | configuration | `infra/tests/test_otel_metrics_pipeline.py::test_collector_service_and_scrape` |
| Backend metrics arrive | configuration | `infra/tests/test_otel_metrics_pipeline.py::test_backend_exports_otlp_and_keeps_actuator` |
| Frontend is scraped | unit + configuration | `frontend/src/tests/unit/metrics-route.test.ts`, `infra/tests/test_otel_metrics_pipeline.py::test_frontend_scrape_job` |
| Container and host targets are up | configuration | `infra/tests/test_otel_metrics_pipeline.py::test_exporters_scraped` |

Plus live verification of every target UP.

- New Java tests: none (properties only). Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh`, `cd frontend && npm test`, `mvn test -pl backend-api`.

## Playwright Strategy

n/a - no UI change; the full Playwright run is the validation of #1469.

## Deployment Strategy

- Flyway: no. `.env`: none. Order: observability stack, then rebuild backend and frontend images.
- Smoke (Gate 5): all new targets UP in Prometheus; backend series visible via the collector.

## Rollback Strategy

- Revert safe: yes. Remove services, scrape jobs, dependencies and route together.

## Migration Plan

1. Failing guards. 2. Collector/exporters/scrape. 3. Backend OTLP. 4. Frontend route. 5. Docs. 6. Live check.
