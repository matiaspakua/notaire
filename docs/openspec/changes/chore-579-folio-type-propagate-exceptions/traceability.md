# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #579 | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/chore-579-folio-type-propagate-exceptions/` | passed |
| Branch | `chore/579_folio-type-propagate-exceptions` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Create folio type unexpected failure returns ErrorResponse | FolioTypeControllerTest.unexpectedCreateFailureUsesGlobalHandler | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| CHANGELOG | deferred (Unreleased fence held by #1990) | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #579, this folder |
| 2 | Failing tests written | pending | FolioTypeControllerTest |
| 3 | Suite green, docs | pending | pending |
| 4 | CI green | pending | pending |
| 5 | Deployed | pending | pending |

## Exceptions

`multi_pr: true` — #579 remains open for other controllers; this PR uses `Refs #579`.
