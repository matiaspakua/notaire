> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#577 ArchUnit forbids entity signatures; loose Object returns remain on a baseline file.

## Goals / Non-Goals

**Goals:** ['Type Item create + Folio update/delete', 'Shrink baseline by 3', 'FolioExceptionHandler']
**Non-Goals:** ['All remaining baseline rows']

## Decisions

['Folio domain exceptions → FolioExceptionHandler like FolioType']

## Riesgos / Trade-offs

['Clients that relied on empty Object generics see no wire change']

## Testing Strategy

['ControllerSignatureArchitectureTest green after baseline shrink', 'FolioExceptionHandlerTest or extend FolioControllerExceptionPropagationTest']

## Regression Strategy

['Existing Folio/Item unit tests']

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
