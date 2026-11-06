> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Continues #579 after management-status (#2017).

## Goals / Non-Goals

**Goals:** ['No catch (Exception) in SubmittedDocumentController', 'Unit proof unexpected create → 500 ErrorResponse']
**Non-Goals:** ['Closing #579', 'ReportController PDF wrap']

## Decisions

['Domain/not-found exceptions already rethrown; remove the outer catch-all entirely']

## Riesgos / Trade-offs

['Delete conflict empty 409 becomes GEH-shaped 500 unless a specific conflict exception exists — same as Folio delete slice']

## Testing Strategy

['SubmittedDocumentControllerExceptionPropagationTest']

## Regression Strategy

['Existing submitted-document tests']

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
