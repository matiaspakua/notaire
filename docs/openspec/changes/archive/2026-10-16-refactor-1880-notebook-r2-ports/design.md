> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Post-#1879 testimony R2 (R2=56). Notebook is next (~10 edges).

## Goals / Non-Goals

**Goals:** Clear ~10 notebook R2 edges without behaviour change
**Non-Goals:** Person R2 (#1881); rewriting FolioRepositoryPort (folio slice)

## Decisions

Move helpers to adapter; Spring Data for notebook/folio/person/deed; delete entity-speaking notebook ports; keep OpsPorts

## Riesgos / Trade-offs

NotebookOpsAdapter / AuxiliaryProtocolOpsAdapter import path updates; unit tests mock Spring Data

## Testing Strategy

NotebookTest, AuxiliaryProtocolServiceTest, AuxiliaryProtocolOpsAdapterTest, HexagonArchitectureTest

## Regression Strategy

Notebook / auxiliary protocol REST unchanged

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
