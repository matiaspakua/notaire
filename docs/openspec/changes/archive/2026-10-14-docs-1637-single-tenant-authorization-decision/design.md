> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Evidence: role_modules is editable but not enforced and no migration seeds roles; JWT_SECRET is per installation with no default.

## Goals / Non-Goals

**Goals:** documents match the Owner decision and point to the work items.
**Non-Goals:** any code change.

## Decisions

1. **Keep the ADR-029 file name** so existing links keep working; the title and status carry the new meaning.

## Riesgos / Trade-offs

- [Other documents still mention object-level checks] → grep for SR-11 across permanent docs was applied.

## Testing Strategy

docs/tests (link and markdown rules) and markdown lint over the changed files.

## Regression Strategy

python3 -m unittest discover -s docs/tests

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
