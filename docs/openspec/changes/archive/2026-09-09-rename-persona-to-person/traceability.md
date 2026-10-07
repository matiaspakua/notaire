# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #974 (epic #973) | in-progress |
| Use Case | N/A — technical refactor, no business-behavior change | n/a |
| Specification | `openspec/changes/rename-persona-to-person/` (`skip_specs: true`) | complete |
| Branch | `refactor/974_rename-persona-to-person` | active |
| Tasks | `tasks.md` | in progress |
| Commits | — | pending |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending (post-deploy) |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| `persona` table/columns renamed via new Flyway migration | `FlywaySchemaValidationIntegrationTest` (`-Ppg-integration`) | pending |
| `Person` CRUD via `/api/v1/people` behaves identically to previous `/api/v1/personas` | `PersonControllerTest` (renamed from `PersonaControllerTest`) | pending |
| Frontend Person pages/components render and submit correctly | `frontend/tests/e2e/*person*.spec.ts` (renamed) | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #974, `proposal.md` |
| 2 | Failing tests written, test cases designed | no | pending |
| 3 | Suite green, coverage held, docs updated | no | pending |
| 4 | CI green, review approved, no conflicts | no | pending |
| 5 | Deployed, smoke test passed, Issue closed | no | pending |

## Exceptions

`skip_specs: true` — pure structural rename, no new or changed business
behavior, so no `specs/*.md` delta is authored per CONSTITUTION.md Gate 1
guidance (spec deltas are for behavior changes).
