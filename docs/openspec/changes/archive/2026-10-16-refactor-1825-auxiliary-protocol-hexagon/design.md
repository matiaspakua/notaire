> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Epic #1693 ArchUnit R1 / ports-only; after Notebook #1817. CU81.

## Goals / Non-Goals

**Goals:** Controller calls inbound ports only; no `business.Deed` import or entity response body.
**Non-Goals:** Domain Deed extraction; FolioRepositoryPort entity cleanup (R2); FE protocol pages.

## Decisions

Mirror Notebook: keep `AuxiliaryProtocolService` for entity logic; `AuxiliaryProtocolOpsAdapter` maps to views and maps validation/not-found into sealed `StartResult`. Controller maps `DeedView` → wire `DeedResponse` (JSON keeps `idDeed` for Bruno).

## Riesgos / Trade-offs

Start-deed 400/404 map bodies must remain; Jackson field names on created deed must stay compatible (`idDeed`, `number`, …).

## Testing Strategy

New unit tests for WriteService / OpsAdapter; existing `AuxiliaryProtocolServiceTest` + `AuxiliaryProtocolControllerTest`; HexagonArchitectureTest shrink-only.

## Regression Strategy

Bruno `api-test/auxiliary-protocol/*`.

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
