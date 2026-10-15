# Traceability

> Governed by [CONSTITUTION.md](../../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1722 | open (PR #1723) |
| Use Case | CU27 / CU30 / CU35 / CU38 / CU65 / CU67 (architectural; epic #1693) | exists |
| Specification | archived `2026-10-15-refactor-1722-shared-catalog-lock` | archived |
| Branch | `refactor/1722_shared_catalog_lock` | created |
| Tasks | groups 3–8 done; archive in this PR | done |
| Commits | `bc0f3b83` (helper+tests); `8c689258` (wire+ADR); archive tip | in PR |
| Pull Request | #1723 | open |
| CI / Merge / Release / Smoke | pending heavy CI | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| locked update/delete keep 409 bodies | Folio/Document/ManagementStatus characterization | green |
| stale version keeps standard 409 | same | green |
| unknown id empty 404 | same | green |
| helper unit tests without Spring | CatalogLockSupportTest (8) | green |
| identification update unlocked; delete via helper | IdentificationTypeApiCharacterizationTest | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | yes | `8c689258` |
| `CHANGELOG.md` | Unreleased Changed | tip |
| `docs/openspec/specs/shared-catalog-lock/spec.md` | created on archive | tip |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1722 |
| 2 | yes | `bc0f3b83` |
| 3 | yes | ADR-030; CHANGELOG held for #1721 |
| 4–5 | pending | merge when `check-heavy-ci.sh 1723` exits 0 |

## Exceptions

CHANGELOG Unreleased added after #1721 merged. Pure internal refactor — no REST behaviour change.
