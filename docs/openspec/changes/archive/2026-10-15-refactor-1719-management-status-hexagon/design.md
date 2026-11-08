> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

Slice 5 of epic #1693. Management status is the fourth locked-when-in-use catalog. Usage is via deed managements. Fields: name, notes, version.

## Goals / Non-Goals

**Goals:** strict hexagon; keep lock/in-use shape for a later shared extract.
**Non-Goals:** CHANGELOG while #1716 is open; extracting the shared catalog rule in this PR; frontend.

## Decisions

1. Explicit version check in the use case (same as folio/document).
2. Legacy entity stays in `business`.
3. Controller-scoped exception advice with highest precedence.
4. Shared-rule extract deferred to a follow-up after this merge.

## Riesgos / Trade-offs

- [Stale-version behaviour] → characterize first; call out any deliberate 409 correction in the PR.
- [Shared extract deferred] → four copies of the rule until the follow-up; acceptable per ADR-030 timing.

## Testing Strategy

`ManagementStatusApiCharacterizationTest` passes before and after; domain, use-case and controller advice tests; ArchUnit shrink.

## Regression Strategy

`mvn test -pl backend-api` including `CatalogControllersIntegrationTest` ManagementStatus nested tests.

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
