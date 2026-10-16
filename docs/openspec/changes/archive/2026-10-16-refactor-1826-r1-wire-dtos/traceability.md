# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1826 | in-progress |
| Use Case | Architectural refactor under epic #1693 (ADR-030) — wire mapping only | exists |
| Specification | `docs/openspec/changes/2026-10-16-refactor-1826-r1-wire-dtos/` | passed |
| Branch | `cursor/1826-r1-wire-dtos-a603` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario | Test | Status |
|----------|------|--------|
| ItemResponse has no business.Item/Budget import | HexagonArchitectureTest | passing |
| *Ref have no business entity imports | HexagonArchitectureTest | passing |
| ErrorResponses has no jakarta.persistence import | HexagonArchitectureTest | passing |
| REST JSON for items unchanged | existing item/budget tests | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| ADR-030 | pending | pending |
| CHANGELOG.md | n/a (#1766) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1826, this folder |
| 2 | Failing tests | pending | pending |
| 3 | Suite green | pending | pending |
| 4 | CI green | pending | pending |
| 5 | Deployed | pending | pending |

## Exceptions

None.
