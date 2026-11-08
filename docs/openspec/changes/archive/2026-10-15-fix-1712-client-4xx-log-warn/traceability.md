# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. It is created during planning with the upstream
> links filled in, and completed as the change moves through the gates. Rows below
> Tasks stay `pending` until the corresponding step actually happens — never
> pre-fill them.

## Chain

```
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1712 (+ #1713 prune) | in-progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-1712-client-4xx-log-warn/` | drafted |
| Branch | `cursor/fix-1712-4xx-log-warn-ba90` | pending |
| Tasks | `tasks.md` | pending |
| Commits | | pending |
| Pull Request | | pending |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| 409 NotaireException logs WARN | `GlobalExceptionHandlerLogLevelTest` | pending |
| 500 NotaireException logs ERROR | `GlobalExceptionHandlerLogLevelTest` | pending |
| Management delete FK conflict logs WARN | controller unit / ListAppender | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CHANGELOG.md` | pending | |
| `docs/openspec/specs/backend-logging/spec.md` | on archive | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | pending | validate-sdlc-plan.sh |
| 2 | Failing tests written, test cases designed | pending | |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

Agent cannot set `in-progress` label or close issues (GitHub integration write limits). PR will note that for a human/Foreman.
