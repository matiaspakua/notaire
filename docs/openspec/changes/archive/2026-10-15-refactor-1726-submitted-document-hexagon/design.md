> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 strangler. Document types (#1705) already provide `DocumentTypeCatalogPort`. A legacy entity-shaped `SubmittedDocumentRepositoryPort` exists but is unused by the controller; replace/extend with domain-typed ports. Existing MockMvc tests cover create/list/update/delete, validation, and partial updates.

## Goals / Non-Goals

**Goals:** ports-only SubmittedDocumentController; domain + use cases; ArchUnit R1 shrink; REST unchanged; no CHANGELOG while #1718 holds lane.
**Non-Goals:** retiring business entity; changing OpenAPI; folio/workflow/FE; inventing new endpoints.

## Decisions

1. Mirror document-type hexagon layout under `domain.document` / `application.port.*.document` / `usecase.document`. 2. Resolve document type via existing `DocumentTypeCatalogPort` (domain). 3. Add a minimal procedure lookup outbound port (id existence + id for association) rather than injecting `ProcedureRepository` into the use case. 4. Keep web request/response records on the controller. 5. Keep create/update try/catch mapping until #579. 6. Omit CHANGELOG.

## Riesgos / Trade-offs

- [Due-date inheritance drift] → characterization + existing integration tests pin behaviour.
- [Procedure lookup port vs coupling] → thin port returning only what association needs.
- [Legacy entity consumers] → leave `business.SubmittedDocument`; list debt callers in PR.

## Testing Strategy

Existing SubmittedDocument* integration tests must stay green (characterization). Add unit tests for domain + use cases with in-memory ports. Shrink hexagon baseline for this controller's R1 lines.

## Regression Strategy

mvn test -pl backend-api -Dtest=SubmittedDocument*,DocumentType*,HexagonArchitectureTest,UseCaseSliceCycleArchitectureTest; mvn verify -pl backend-api when feasible; Bruno only if OpenAPI drifts (prefer empty).

## Playwright Strategy

n/a — no UI surface change.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None.
