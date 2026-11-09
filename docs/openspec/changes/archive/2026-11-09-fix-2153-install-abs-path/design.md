> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Notaire factory validation after #2152.

## Goals / Non-Goals

**Goals:** Fail closed on absolute install paths.
**Non-Goals:** Supporting installs outside the project root.

## Decisions

Check Path.is_absolute before strip('/'); omit CHANGELOG while Release Please #2150 holds fence.

## Riesgos / Trade-offs

None — tighter validation only.

## Testing Strategy

InstallAbsolutePathTest

## Regression Strategy

InstallLifecycleTest

## Playwright Strategy

n/a

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
