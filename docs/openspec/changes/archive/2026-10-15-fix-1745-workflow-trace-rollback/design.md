> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Filed from Mac #1740 smoke; related #1736 log-noise DoD.

## Goals / Non-Goals

**Goals:** Eliminate UnexpectedRollbackException ERROR spam on workflow-trace 400 path.
**Non-Goals:** #1746 grafana panels; full controller exception rewrite.

## Decisions

1. Remove controller @Transactional; keep service readOnly TX.
2. Prefer boundary fix over demoting UnexpectedRollbackException logs.

## Riesgos / Trade-offs

Lazy-loading must stay inside WorkflowTraceService TX — already annotated.

## Testing Strategy

Failing unit reflection + H2 integration with log appender; then remove annotation.

## Regression Strategy

Existing WorkflowTraceApiH2IntegrationTest 200/400 cases stay green.

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
