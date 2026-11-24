> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

generate.mjs copied LikeC4's relationship id into the findings list; that id is a hash that includes the document location.

## Goals / Non-Goals

**Goals:** Deterministic generated data.
**Non-Goals:** Changing the model.

## Decisions

D1 Build the id from the source FQN, the target FQN and the title, which are unique for a finding.

## Riesgos / Trade-offs

None.

## Testing Strategy

`test_finding_ids_do_not_depend_on_the_checkout` (red, then green); regenerated in two worktrees, identical output.

## Regression Strategy

`test_architecture_model.py`, Pages Vitest `architecture-model.test.tsx`.

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
