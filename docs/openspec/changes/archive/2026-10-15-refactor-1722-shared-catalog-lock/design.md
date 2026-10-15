> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — §6 Quality Gates.

## Context

After #1719 / #1720, four catalogs implement locked-when-in-use. Folio, document and management status share nearly identical `update` / `delete` control flow in their management services. Identification differs (name uniqueness; lock only on delete).

## Goals / Non-Goals

**Goals:** one shared helper for the write lock + version check; zero HTTP behaviour change; ADR-030 log.
**Non-Goals:** CHANGELOG while #1721 is open; REST or message-text changes; frontend; new catalogs.

## Decisions

1. **Shared collaborator in `application` (not domain).** The rule coordinates ports and version comparison; domain types and exception messages stay catalog-owned. Prefer a small package such as `application.usecase.catalog` (or `application.support`) with a static/helper API that takes:
   - load-by-id
   - usage predicate (`isInUse`)
   - exception factories for not-found / locked-for-edit / cannot-delete / stale-version
   - apply-change + save (update) or delete-by-id (delete)
2. **Do not unify domain exception types.** Each catalog keeps its `*InUseException` messages; the helper only invokes the supplied factories so OpenAPI clients see the same bodies.
3. **Identification:** adopt the **delete** half of the helper if it maps cleanly without changing the empty/special delete body; leave create/update (name uniqueness) local. Document the choice in the ADR log.
4. **Tests first:** unit tests for the helper with fakes; then refactor services; then re-run each catalog's characterization / referential suite.
5. **CHANGELOG omitted** until #1721 merges.

## Riesgos / Trade-offs

- [Over-abstraction] → keep the helper under ~40 lines; no generics soup; three call sites must stay readable.
- [Identification asymmetry] → do not force update-lock onto identification; that would be a behaviour change.
- [Silent body drift] → characterization suites are the contract; fail the PR if any 409/404 shape changes.

## Testing Strategy

- New `CatalogLockSupportTest` (or similar) with in-memory fakes: happy update, locked update, stale version, happy delete, locked delete, missing id.
- Re-run: `FolioTypeApiCharacterizationTest`, `DocumentTypeApiCharacterizationTest`, `ManagementStatusApiCharacterizationTest`, identification referential / characterization equivalents.
- ArchUnit: no new R1–R6 violations; shrink if possible.

## Regression Strategy

`mvn test -pl backend-api` focused on catalog suites; `SimpleControllersTest` unaffected.

## Playwright Strategy

n/a — no UI surface.

## Deployment Strategy

Ships with the next release.

## Rollback Strategy

Revert the PR.

## Migration Plan

None.

## Open Questions

None — identification delete adoption is a design choice recorded above (prefer adopt delete half only).
