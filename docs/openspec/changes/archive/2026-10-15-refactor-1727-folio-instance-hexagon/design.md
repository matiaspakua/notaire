> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Folio types (#1703) done. FolioController is the largest folio R1 hotspot.

## Goals / Non-Goals

**Goals:** ports-only FolioController; ArchUnit shrink; REST unchanged; no CHANGELOG while #1718 holds lane.
**Non-Goals:** retiring business.Folio; migrating AuxiliaryProtocol.

## Decisions

1. FolioCatalogPort alongside FolioRepositoryPort. 2. PersonLookupPort + DeedLookupPort. 3. Reuse FolioTypeCatalogPort. 4. Omit CHANGELOG.

## Riesgos / Trade-offs

- [Update returns entity JSON] → FolioResponse mapping; tests pin status/deed.
- [Cascade on delete] → keep remove-from-collection in adapter.

## Testing Strategy

FolioControllerTest + FolioRequestValidationIntegrationTest; unit use-case tests; ArchUnit shrink.

## Regression Strategy

mvn test -pl backend-api -Dtest=Folio*,HexagonArchitectureTest,UseCaseSliceCycleArchitectureTest

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
