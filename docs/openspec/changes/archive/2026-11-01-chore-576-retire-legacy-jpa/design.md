> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates,
> §7 Testing Rules, §11 Release Rules.

## Context

ArchUnit R1–R6 baseline is empty (#1914/#1915). Residual documented in ADR-030 /
#1918: `jpa/*JpaController` + `JpaAdministrator` + `LegacyJpaEmfConfig` exist only
to seed `IdentificationTypeLookup` via `IdentificationTypeJpaController.findIdentificationTypeEntities()`.
Spring Data `IdentificationTypeRepository` and hexagonal catalog ports already exist.

## Goals / Non-Goals

**Goals:**
- Delete the entire `com.licensis.notaire.jpa` package and the two `service` bootstrap classes
- Keep `IdentificationTypeLookup.shared()` working for remaining `business` entity DTO helpers
- Keep ArchUnit R1–R6 empty; REST contract unchanged

**Non-Goals:**
- Entity→domain extraction (#580)
- Controller try/catch cleanup (#579)
- Closing epic #1693 if other DoD rows remain (document residual honestly)

## Decisions

1. **Catalog wire:** replace `LegacyJpaEmfConfig` with
   `IdentificationTypeLookupConfig` that calls
   `IdentificationTypeLookup.wireCatalog(identificationTypeRepository::findAll)`.
   Keeps the existing static API used by `DeedManagement` / `Procedure` without
   pulling Spring into `business`.
2. **Hard delete** of `jpa` and the two service classes in one PR — measured as a
   dead island (no production callers outside those packages). Alternative of
   strangling one controller per PR adds cost with no behaviour change.
3. **DtoOwnershipTest** drops the `PreexistingEntityException` sample (legacy-only
   type); keep other DTO samples.
4. Remove obsolete JaCoCo excludes for deleted packages so the ratchet measures
   remaining code only.

## Riesgos / Trade-offs

- [Risk] Hidden reflection/classpath use of `*JpaController` → Mitigation: full
  unit + pg-integration + Bruno/preflight; search for FQCNs before delete.
- [Risk] `IdentificationTypeLookup` empty before repository bean ready → Mitigation:
  wire in `@Configuration` method with repository injected (same lifecycle as
  former EMF setter).
- [Trade-off] Static shared lookup remains (entity DTO helpers); pure ports would
  wait on #580.

## Testing Strategy

- New `LegacyJpaRetirementTest`: asserts `Class.forName` fails for
  `com.licensis.notaire.jpa.PersonJpaController`, `…jpa.IdentificationTypeJpaController`,
  `…service.JpaAdministrator`, `…service.LegacyJpaEmfConfig`.
- Extend/keep `IdentificationTypeLookupTest` for wireCatalog behaviour.
- `HexagonArchitectureTest` must stay green with empty baseline.
- Observe red before delete; green after.

## Regression Strategy

- Existing identification-type / person / management unit and integration tests
- Backend verify + JaCoCo check (excludes cleaned)
- No OpenAPI diff expected

## Playwright Strategy

n/a — no UI surface; REST paths and JSON unchanged.

## Deployment Strategy

Ships with the next release cut after merge.

## Rollback Strategy

Revert the PR; packages return from git history.

## Migration Plan

None (no schema or API migration).

## Open Questions

None.
