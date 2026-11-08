> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. After #2125 TelemetryPort and #2135 install.

## Goals / Non-Goals

**Goals:** Measurable lead time and stage durations for the factory.
**Non-Goals:** Live GitHub polling; ROI product UI; CHANGELOG fence while #2145 open.

## Decisions

Fixture-based forge sample; merge not required; TelemetryPort emit when bound.

## Riesgos / Trade-offs

Fixture drift from real forge clocks — document sample as illustrative.

## Testing Strategy

test_metrics.py; verify.sh; Notaire sample CLI

## Regression Strategy

evidence report/cost tests

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
