> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

SEMVER service.version already on main via #1591/ADR-028. Epic #1461 delivered the stack; this change fixes silent/empty panels from misconfig.

## Goals / Non-Goals

**Goals:** Meaningful Postgres and quality Grafana panels; version identity on dashboards; zero piechart plugin log noise.
**Non-Goals:** External alert receivers; K8s observability sidecars; inventing Owner product scope.

## Decisions

Keep extend.query-path (deprecated but still the supported custom-SQL hook in postgres-exporter v0.20) rather than rewriting business gauges as Go collectors. Use Micrometer version tag for Grafana (Actuator scrape path) in addition to OTel service.version.

## Riesgos / Trade-offs

Existing Grafana volumes may need recreate to pick up dashboards; exporter recreate required for extend-query path; business gauges need pg_monitor SELECT on English tables.

## Testing Strategy

TDD: infra/tests/test_obs_dashboard_wiring.py red then green; infra/verify.sh; ProductVersionTelemetryTest; workspace/tests/test_release_runtime_identity.py.

## Regression Strategy

Existing dashboard UIDs unchanged except additive notaire-quality; Homer still lists every compose service.

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
