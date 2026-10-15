> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Filed from Mac #1740 smoke; prior wiring #1707/#1711.

## Goals / Non-Goals

**Goals:** Fill the three empty notaire-postgres panels and stop exporter permission ERROR spam.
**Non-Goals:** Mac re-run; full #1735 panel screenshot campaign; inventing new Micrometer DB timers.

## Decisions

1. Reshape extend-query group/column to group=`X` column=`total` → scraped `X_total`.
2. Flyway GRANT SELECT only (not DML).
3. Ops-rate panel switches to rate(notaire_operation_total) rather than wiring unused recordDatabaseOperation.

## Riesgos / Trade-offs

Existing deployments need V51 applied before business panels fill; exporter restart after query file change.

## Testing Strategy

Failing infra unittest guards first (metric naming, grants migration text, ops-rate PromQL), then implement.

## Regression Strategy

test_obs_dashboard_wiring / test_postgres_exporter_english stay green after key reshape.

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
