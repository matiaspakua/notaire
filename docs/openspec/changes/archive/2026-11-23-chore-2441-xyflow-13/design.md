> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Only workflows/[id]/page.tsx used applyNodeChanges; v13 onNodesChange receives a NodeChangeset.

## Goals / Non-Goals

**Goals:** Upgrade with no behaviour change.
**Non-Goals:** New editor features.

## Decisions

changes.applyTo(nodes) replaces applyNodeChanges(changes, nodes); the local position map is unchanged.

## Riesgos / Trade-offs

Medium-low: editor drag/selection; covered by TS-0147..0152 Playwright specs.

## Testing Strategy

Vitest guard (red on 12, green on 13); frontend typecheck, unit suite, build; Playwright TS-0147..TS-0152.

## Regression Strategy

Workflow node and edge unit tests; workflow editor Playwright specs.

## Playwright Strategy

testing/e2e/tests/TS-0147-workflow-editor-layout.spec.ts

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
