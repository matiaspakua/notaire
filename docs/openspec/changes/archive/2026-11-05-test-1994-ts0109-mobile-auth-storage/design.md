> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

PR #1991 moved *mobile* specs to the WebKit iPhone SE project with shared storageState; #1993 CI showed TS-0109 stuck on Cargando.

## Goals / Non-Goals

**Goals:** ['Green TS-0109 on CI shard with mobile project', 'Reliable admin-auth.json for all projects using storageState']
**Non-Goals:** ['Changing dashboard auth UX', 'Removing mobile project']

## Decisions

['Use page.evaluate to set localStorage after cookie set (initScript alone may not run before storageState save)', 'Keep TS-0109 on storageState; assert skeleton clears']

## Riesgos / Trade-offs

['storageState file shape changes — covered by TS-0109']

## Testing Strategy

['TS-0109-mobile-lists.spec.ts under project mobile']

## Regression Strategy

['chromium project unrelated specs']

## Playwright Strategy

TS-0109-mobile-lists

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
