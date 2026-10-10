> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6, §7, §11.

## Context

See [proposal.md](proposal.md). `logback-spring.xml` already includes MDC `traceId`/`spanId`, but no code logs per request.

## Goals / Non-Goals

**Goals:** one correlated line per request in both modules; log-to-trace link. **Non-Goals:** client beacon (#1483), frontend spans.

## Decisions

1. **A servlet `OncePerRequestFilter` in the backend**, ordered after Spring's observation filter so the span is active
   (MDC populated). Rejected: Tomcat access log (not in MDC scope, different format); `CommonsRequestLoggingFilter` (logs payload/query).
2. **Log method + path only.** Query strings and headers can carry identifiers or tokens. Path IDs are already in audit logs.
3. **Frontend: reuse `lib/logger.ts`** (JSON mirroring the backend schema) inside `backend-proxy.ts`; no new dependency.
4. **Derived field on the Loki datasource** (regex on `"traceId":"(\w+)"`), not a promoted label, to avoid high-cardinality labels.

## Riesgos / Trade-offs

- [Log volume] one INFO line per request -> acceptable; Actuator excluded.
- [Filter order] if the span is not active the line has no traceId -> covered by a test asserting MDC propagation.

## Testing Strategy

| Scenario (spec) | Test level | Test |
|-----------------|------------|------|
| API request is logged with its trace id | unit | `RequestLoggingFilterTest::shouldLogRequestWithTraceIdFromMdc` |
| Sensitive and noisy data is excluded | unit | `RequestLoggingFilterTest::shouldNotLogQueryString`, `::shouldSkipActuatorPaths` |
| Proxied request is logged | unit | `backend-proxy.test.ts` (logs a structured line) |
| Upstream failure is logged as an error | unit | `backend-proxy.test.ts` (ERROR line, 502) |
| Derived field is provisioned | configuration | `infra/tests/test_log_trace_correlation.py` |

- New Java tests: `RequestLoggingFilterTest` (unit). Coverage impact: positive (new class fully covered).

## Regression Strategy

- `mvn test -pl backend-api`, `cd frontend && npm test`, `bash infra/verify.sh`.

## Playwright Strategy

n/a - no UI change; full E2E run is validated in #1469.

## Deployment Strategy

- Flyway: no. `.env`: none. Rebuild backend and frontend images; restart Grafana (datasource provisioning is read at startup).
- Smoke (Gate 5): a request's log line in Loki carries a `traceId` that opens the trace in Tempo.

## Rollback Strategy

- Revert safe: yes (filter, logger calls and one datasource block).

## Migration Plan

1. Failing tests. 2. Backend filter. 3. Frontend proxy logging. 4. Datasource. 5. Docs. 6. Live check.
