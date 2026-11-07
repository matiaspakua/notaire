> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md).

## Context

#577 Concept uses ResponseEntity<?> for writes.

## Goals / Non-Goals

**Goals:** ['Type create/update/delete', 'Shrink baseline by 3']
**Non-Goals:** ['Closing #577']

## Decisions

['create keeps Map body keys fkIdProcedureType/fkIdConcept']

## Riesgos / Trade-offs

['No wire change']

## Testing Strategy

['ControllerSignatureArchitectureTest', 'existing ConceptControllerTest']

## Regression Strategy

['ConceptControllerTest']

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
