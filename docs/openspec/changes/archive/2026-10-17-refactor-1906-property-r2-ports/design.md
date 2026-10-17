> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Child of #1693 after user #1905 merged (R2=1 on main @ 2d7c48cda).

## Goals / Non-Goals

**Goals:** Clear the last R2 edge (property) without behaviour change.
**Non-Goals:** R6; FE changes; API contract changes.

## Decisions

`ProcedureOpsAdapter` already lives under `adapter.out.persistence.procedure`. Inject Spring Data `PropertyRepository` directly; delete unused entity-speaking `PropertyRepositoryPort` and `PropertyPersistenceAdapter`; keep domain `PropertyCatalogPort`.

## Riesgos / Trade-offs

None material — one injector swap.

## Testing Strategy

HexagonArchitectureTest baseline shrink; existing procedure/property catalog coverage.

## Regression Strategy

Existing procedure Bruno/API and property catalog tests.

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
