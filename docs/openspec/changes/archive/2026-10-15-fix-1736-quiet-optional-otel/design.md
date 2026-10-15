> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Bruno and local smoke still see OTEL UnknownHostException ERROR and Hibernate Temporal WARN after #1712/#1715.

## Goals / Non-Goals

**Goals:** No ERROR spam when the collector is absent; no Temporal deprecation WARN flood; CHANGELOG-free.
**Non-Goals:** Entity java.time migration; FE tracing; changing Actuator scrape.

## Decisions

1. Default OTLP export off (opt-in via env) rather than log-level demotion — stops the failed export loop.
2. Suppress Temporal deprecation via logger level; migration deferred to hex/entity work.

## Riesgos / Trade-offs

Operators running the backend outside Compose without setting enable flags lose OTLP until they set env — Actuator scrape still works.

## Testing Strategy

Failing unit property guard first; then implement; `python3` OpenSpec validate; targeted Maven test.

## Regression Strategy

ProductVersionTelemetryTest and TracingClasspathTest still pass; compose still injects collector URLs.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
