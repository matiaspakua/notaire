> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

After #1711/#1746 panel wiring; Mac #1740 proved API signals only.

## Goals / Non-Goals

**Goals:** Close the Grafana useful-data DoD gap with wiring + live assert.
**Non-Goals:** FE OTel; re-owning #1740; CHANGELOG entry.

## Decisions

1. Assert panel PromQL via Prometheus (same series Grafana renders) plus Grafana API dashboard UID presence.
2. CHANGELOG-free per issue AC.
3. Optional Sonar quality probes may SKIP if down.

## Riesgos / Trade-offs

Live script needs stack up; CI unit tests stay offline.

## Testing Strategy

Failing wiring test first, then inventory + script + shell flag.

## Regression Strategy

Existing verify_signals and dashboard wiring tests stay green.

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
