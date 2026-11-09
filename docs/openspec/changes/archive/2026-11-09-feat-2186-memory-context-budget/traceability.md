# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #2186 | open |
| Use Case | CU76 | exists |
| Specification | `docs/openspec/changes/feat-2186-memory-context-budget/` | passed |
| Branch | `cursor/feat-2186-memory-context-budget-10f2` | created |
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
| omitted memory is no-op | MemoryContextBudgetTest | pending |
| budget configured via memory packs only | MemoryContextBudgetTest | pending |
| unknown memory pack fails validate | MemoryContextBudgetTest | pending |
| unknown memory pack fails doctor | MemoryContextBudgetTest | pending |
| missing catalogs dir with memory ref fails | MemoryContextBudgetTest | pending |
| budget includes pack ref bytes | MemoryContextBudgetTest | pending |
| missing pack ref fails budget | MemoryContextBudgetTest | pending |
| hostile pack summary never executes | MemoryContextBudgetTest | pending |
| config show includes context memory | MemoryContextBudgetTest | pending |
| context memory resolves via catalog entry | MemoryContextBudgetTest | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/README.md` | no | pending |
| `framework/ai-sdlc/templates/project/aisdlc.toml` | no | pending |
| `docs/300-development/306-ai-sdlc-framework/FACTORY-ROADMAP.md` | no | pending |
| `CHANGELOG.md` | no | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #2186, this folder |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

None. Label flip to `status:in-progress` may fail (integration GraphQL); noted in PR.
