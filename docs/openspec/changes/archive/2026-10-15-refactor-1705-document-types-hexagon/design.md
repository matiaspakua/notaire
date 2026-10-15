> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slice 4 of epic #1693. Document types mirror folio types and identification types, with two differences: usage spans procedure templates **and** submitted documents, and the domain record carries more fields. The client sends the version it read; the use case compares versions explicitly (same decision as slice 3).

## Goals / Non-Goals

**Goals:** fourth catalog on the strict hexagon pattern; keep the lock/in-use shape so a shared rule can be extracted after management status.
**Non-Goals:** changing validation or response shapes; moving the legacy entity out of `business`; frontend hexagon; extracting the shared catalog rule now.

## Decisions

1. **Dual usage port** — `DocumentTypeUsagePort` answers whether the type is referenced by procedure templates or submitted documents (one method is enough for the lock/delete rules).
2. **Explicit version check in the use case** — same as folio types.
3. **Keep the DTO as the controller's wire shape** so OpenAPI is untouched.
4. **Legacy entity stays in `business`** until every consumer migrates (listed as debt on #1705).
5. **Controller-scoped exception advice** with highest precedence maps domain exceptions to byte-identical 404/409 bodies (pattern from #1700/#1703).
6. **Create defaults** stay in the inbound adapter (wire defaults), not the domain — matching the old controller.

## Riesgos / Trade-offs

- [Dual-table in-use] → characterization covers both procedure-template and submitted-document references.
- [More fields than folio] → domain factories cover create/restore/changeTo; mapper unit-tested.
- [Optimistic locking / 409 bodies] → pinned before and after by characterization.

## Testing Strategy

`DocumentTypeApiCharacterizationTest` passes on the old code and stays green; domain, use-case (in-memory) and adapter/mapper tests; ArchUnit shrink; `mvn verify -pl backend-api`. Read build exit codes and fresh logs, not stale surefire reports.

## Regression Strategy

`mvn verify -pl backend-api`; Bruno suite if OpenAPI export drifts (expect empty diff).

## Playwright Strategy

n/a — no UI surface in this slice.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None (strangler; entity remains).

## Open Questions

None.
