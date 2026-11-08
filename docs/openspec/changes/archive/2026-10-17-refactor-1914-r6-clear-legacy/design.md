> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

After property #1906 merged (R2=0 on main @ f8ce6502e). Only R6=2 remains.

## Goals / Non-Goals

**Goals:** R6 → 0; R1–R5 stay empty.
**Non-Goals:** Retiring jpa/service packages entirely; FE work.

## Decisions

1. IdentificationTypeLookup: injectable catalog via `wireCatalog`, installed from `JpaAdministrator.setEmf` (service may depend on jpa).
2. Move Spring EMF bootstrap into `service.LegacyJpaEmfConfig`; delete `config.JpaConfig`.

## Riesgos / Trade-offs

shared() returns empty catalog until EMF is set — same timing as before (supplier was lazy on getInstance).

## Testing Strategy

HexagonArchitectureTest (empty baseline) + IdentificationTypeLookupTest (wireCatalog) + JpaAdministratorTest.

## Regression Strategy

Existing app boot / JPA integration paths.

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
