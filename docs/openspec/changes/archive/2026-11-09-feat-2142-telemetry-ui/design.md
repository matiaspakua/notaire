> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Parent #2134. Depends on TelemetryPort #2125 and lead-time #2138 (done).

## Goals / Non-Goals

**Goals:** Smallest useful read-only monitor: static HTML from aisdlc report.
**Non-Goals:** Next.js product UI; Port builder; live OTLP dashboards; write/mutate paths.

## Decisions

Extend report CLI with --html; stdlib html.escape; optional --lead-time-fixture; omit CHANGELOG Unreleased while Release Please holds fence.

## Riesgos / Trade-offs

Operators may treat HTML as live telemetry — document it as a snapshot of the ledger.

## Testing Strategy

test_report_html.py against temp repos and lead-time fixture

## Regression Strategy

existing report --json and metrics lead-time tests

## Playwright Strategy

n/a — kit static HTML, not product UI

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
