> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after concept #1899 merged (R2=7 on main @ 5d9aecea4).

## Goals / Non-Goals

**Goals:** Clear ~2 folio R2 edges without behaviour change.
**Non-Goals:** Item/user/property R2; R6; FE changes; API contract changes.

## Decisions

`AuxiliaryProtocolService` already lives under `adapter.out.persistence.notebook`. Inject Spring Data `FolioRepository` directly there; delete unused entity-speaking `FolioRepositoryPort` and its `FolioPersistenceAdapter`; keep domain `FolioCatalogPort` / `FolioTypeCatalogPort` for the ports-only HTTP path.

## Riesgos / Trade-offs

`AuxiliaryProtocolServiceTest` mocks `FolioRepositoryPort` — switch to `FolioRepository`. English path pins in `docs/tests/test_numbering_names_english.py` reference the deleted files — retarget.

## Testing Strategy

`AuxiliaryProtocolServiceTest` + `HexagonArchitectureTest` baseline shrink.

## Regression Strategy

Existing folio catalog / auxiliary-protocol controller and Bruno coverage.

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
