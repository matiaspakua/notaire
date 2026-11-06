> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

#577 ArchUnit forbids entity signatures; loose Object returns remain on a baseline file. FolioType and DocumentType already map domain exceptions via dedicated `@RestControllerAdvice`.

## Goals / Non-Goals

**Goals:** ['Type FolioType + DocumentType create/update/delete', 'Shrink baseline by 6']
**Non-Goals:** ['ConceptController SaveResult Object returns', 'Closing #577']

## Decisions

['Reuse existing FolioTypeExceptionHandler / DocumentTypeExceptionHandler — no new advice']

## Riesgos / Trade-offs

['No wire change; OpenAPI types become accurate']

## Testing Strategy

['ControllerSignatureArchitectureTest green after baseline shrink']

## Regression Strategy

['Existing FolioType/DocumentType controller and handler tests']

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
