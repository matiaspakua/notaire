> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Role Object returns from controller catch blocks.

## Goals / Non-Goals

**Goals:** ['RoleExceptionHandler', 'Type create + assign', 'Shrink baseline by 2']
**Non-Goals:** ['Closing #577']

## Decisions

['Public handler so unit package tests can wire it']

## Riesgos / Trade-offs

['Wire shape unchanged']

## Testing Strategy

['Handler MockMvc tests', 'Wire advice into RoleControllerTest', 'ArchUnit']

## Regression Strategy

['Existing RoleControllerTest']

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Next release.

## Rollback Strategy

Revert PR.

## Migration Plan

None.

## Open Questions

None.
