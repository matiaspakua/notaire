# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1722 | open (implementation) |
| Use Case | CU27 / CU30 / CU35 / CU38 / CU65 / CU67 (architectural; epic #1693) | exists |
| Specification | `docs/openspec/changes/refactor-1722-shared-catalog-lock/` | implementing |
| Branch | `refactor/1722_shared_catalog_lock` | created |
| Tasks | `tasks.md` | groups 3–8 largely done; CI deferred |
| Commits | `bc0f3b83` (helper+tests); wiring+ADR pending | in progress |
| Pull Request | #1723 | open draft |
| CI / Merge / Release / Smoke | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| locked update/delete keep 409 bodies | Folio/Document/ManagementStatus characterization | green re-run |
| stale version keeps standard 409 | same characterization suites | green re-run |
| unknown id empty 404 | same | green re-run |
| helper unit tests without Spring | CatalogLockSupportTest (8) | green |
| identification update stays unlocked; delete via helper | IdentificationTypeApiCharacterizationTest | green re-run |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | yes (extract log) | pending |
| `CHANGELOG.md` | skipped (#1721) | n/a |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1722, this folder |
| 2 | yes | CatalogLockSupportTest first commit `bc0f3b83` |
| 3 | yes | ADR-030 extract note; CHANGELOG held for #1721 |
| 4–5 | pending | pending |

## Exceptions

CHANGELOG omitted while #1721 is open (Owner serialize preference). Pure internal refactor — no REST behaviour change expected.
