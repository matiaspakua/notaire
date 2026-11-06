> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#577 loose Object returns. IdentificationType update/delete already typed; create leftover. Linked worktrees do not inherit the primary checkout `.env`, so compose smoke fails on missing `JWT_SECRET`.

## Goals / Non-Goals

**Goals:** ['Type IdentificationType#create', 'Document worktree .env copy']
**Non-Goals:** ['Closing #577']

## Decisions

['Reuse IdentificationTypeExceptionHandler', 'Copy .env into each worktree before preflight --full']

## Riesgos / Trade-offs

['No wire change']

## Testing Strategy

['ControllerSignatureArchitectureTest after baseline shrink']

## Regression Strategy

['Existing IdentificationType controller/handler tests']

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
