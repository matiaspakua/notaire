> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#1981 found during the technology assessment: workflow filename typo.

## Goals / Non-Goals

**Goals:** Correct filename + references + ratchet test.
**Non-Goals:** Change cron schedule or cleanup policy.

## Decisions

1. Rename only; keep workflow `name:` and jobs identical.
2. Ratchet searches for the typo filename after stripping correct `cleanup-actions.yml`
   mentions (the correct name contains the typo as a letter-suffix substring).

## Riesgos / Trade-offs

- [Risk] External bookmarks to the old path → Mitigation: GitHub resolves workflow by
  content; schedule continues.

## Testing Strategy

`test_cleanup_actions_workflow_name` red → green.

## Regression Strategy

Process Checks discover workspace/tests.

## Playwright Strategy

n/a — no UI product surface (Pages copy only).

## Deployment Strategy

Next merge after #1992 heavy CI frees.

## Rollback Strategy

Revert rename.

## Migration Plan

None.

## Open Questions

None.
