> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slice of umbrella #579 after payment, folio-type, property tips.

## Goals / Non-Goals

**Goals:** ['FolioController has no catch (Exception)', 'Unit test proves GEH path']
**Non-Goals:** ['Changing FolioInvalid/InUse/NotFound HTTP mapping']

## Decisions

['Keep domain exception catches in the controller for this slice']

## Riesgos / Trade-offs

['Delete path previously returned empty 409 on unexpected errors; now 500 — intentional #579']

## Testing Strategy

['FolioControllerTest']

## Regression Strategy

['Folio domain exception paths']

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
