> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). The collector (#1462) is the single OTLP ingress.

## Goals / Non-Goals

**Goals:** traces stored in Tempo and explorable in Grafana. **Non-Goals:** frontend spans, log correlation fields.

## Decisions

1. **Backend sends to the collector, which forwards to Tempo.** One ingress; swapping the trace store later needs no app change. Rejected: backend direct to Tempo.
2. **Replace Brave/Zipkin with the OTel tracer** (`spring-boot-starter-opentelemetry`). Keeping both would produce two tracers. Rejected: Zipkin reporter to Tempo's Zipkin receiver (keeps a legacy path).
3. **Tempo with local filesystem storage and 24h retention**, single binary. Rejected: object storage (overkill locally).
4. **Datasource `tracesToLogsV2` to Loki by `service`** so a trace links to logs.

## Riesgos / Trade-offs

- [Sampling 1.0 volume] -> acceptable locally; retention bounds disk.
- [Tempo down] -> OTLP export errors are logged, not thrown.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| Collector forwards traces | configuration | `infra/tests/test_tempo_traces.py::test_collector_traces_pipeline_to_tempo` |
| A request produces a trace | configuration + live | `infra/tests/test_tempo_traces.py::test_backend_uses_otel_tracer` |
| Datasource is provisioned | configuration | `infra/tests/test_tempo_traces.py::test_grafana_tempo_datasource` |

- New Java tests: none. Coverage impact: none.

## Regression Strategy

- `bash infra/verify.sh`, `mvn test -pl backend-api`.

## Playwright Strategy

n/a - no UI change; validated in #1469.

## Deployment Strategy

- Flyway: no. `.env`: none. Order: observability stack, then rebuild backend.
- Smoke (Gate 5): a request's trace id is retrievable from Tempo.

## Rollback Strategy

- Revert safe: yes. Restore brave/zipkin deps, remove Tempo and the pipeline together.

## Migration Plan

1. Failing guards. 2. Tempo + collector pipeline + datasource. 3. Backend tracer. 4. Docs. 5. Live trace check.
