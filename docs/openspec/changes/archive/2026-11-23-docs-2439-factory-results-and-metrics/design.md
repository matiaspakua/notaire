> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Snapshots: ai-sdlc-history 2026-10-28, delivery-metrics 2026-11-10; RepoLens KPIs end 2026-11-19.

## Goals / Non-Goals

**Goals:** Current, sourced numbers and docs.
**Non-Goals:** New metrics or generators.

## Decisions

Use the existing generators; append dated entries instead of rewriting history; commit the deck as the Owner's reference for the diagram.

## Riesgos / Trade-offs

Low: docs, data snapshots and one ignore list.

## Testing Strategy

Kit test_store (red then green); Pages factory-status test (red then green); markdownlint on touched files.

## Regression Strategy

Pages vitest suite and kit store/serve-token tests stay green.

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
