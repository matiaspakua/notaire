> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after templates admin.

## Goals / Non-Goals

**Goals:** Clear four eslint suppressions without changing REST or UI behaviour.
**Non-Goals:** Other FE catalogs; CHANGELOG.

## Decisions

Single core/workflow covering definitions, nodes, transitions, validate, and management workflow-trace.

## Riesgos / Trade-offs

Merge conflict with open FE catalog PRs on eslint-suppressions and architecture-boundaries pins — resolve by keeping shrink-only counts.

## Testing Strategy

Vitest: use cases + gateway + architecture pin; existing workflow unit tests.

## Regression Strategy

Admin workflows Playwright flows.

## Playwright Strategy

n/a — no UI behaviour change

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
