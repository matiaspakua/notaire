> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after history ServerError (#2030).

## Goals / Non-Goals

**Goals:** ['Empty 500 ServerError → GEH on Substitution create/update', 'Unit proof']
**Non-Goals:** ['Closing #579']

## Decisions

['Throw IllegalStateException so GEH owns the ErrorResponse']

## Riesgos / Trade-offs

['Clients that only checked status remain fine']

## Testing Strategy

['SubstitutionControllerServerErrorResponseTest']

## Regression Strategy

['Existing substitution tests']

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
