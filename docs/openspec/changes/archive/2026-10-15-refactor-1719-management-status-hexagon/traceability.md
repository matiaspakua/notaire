# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1719 | open (implementation on branch) |
| Use Case | CU30 / CU35 / CU67 | exists |
| Specification | `docs/openspec/changes/refactor-1719-management-status-hexagon/` | Gate 1 + implemented |
| Branch | `refactor/1719_management_status_hexagon` | pushed |
| Tasks | `tasks.md` | implementation complete; CI pending |
| Commits | `93ee36f3` (Gate 1), `be117fc1` (domain/ports), `a385a2b2` (controller/tests), `18a2970f` (archive) | done |
| Pull Request | [#1720](https://github.com/matiaspakua/notaire/pull/1720) | draft |
| CI / Merge / Release / Smoke | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| read search and in-use | ManagementStatusApiCharacterizationTest | green |
| locked when referenced (edit/delete 409) | ManagementStatusApiCharacterizationTest | green |
| stale version | ManagementStatusApiCharacterizationTest / ManagementStatusUseCasesTest | green (deliberate 409 correction) |
| use cases without Spring | ManagementStatusUseCasesTest | green |
| architecture baseline shrinks | HexagonArchitectureTest (−3 R1 entries) | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/200-architecture/202-ADR/ADR-030-hexagonal-architecture-full-adoption.md` | yes (slice 5 log) | `a385a2b2` |
| `docs/300-development/303-testing/INVENTORY.md` | yes (regenerated) | `a385a2b2` |
| `CHANGELOG.md` | skipped (#1716) | n/a |

## Gate log

| Gate | Passed | Evidence |
|------|--------|----------|
| 1 | yes | #1719, this folder |
| 2 | yes | characterization + unit tests observed green after implement |
| 3 | partial | ADR + INVENTORY; CHANGELOG deferred (#1716) |
| 4–5 | pending | PR / CI / merge |

## Exceptions

CHANGELOG omitted while #1716 is open (Owner serialize preference). Shared lock/in-use extract deferred to a follow-up after this green merge.
