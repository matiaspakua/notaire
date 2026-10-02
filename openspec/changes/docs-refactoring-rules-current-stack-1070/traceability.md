# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1070 | open (IN PROGRESS label blocked: `gh` write denied for this agent) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/docs-refactoring-rules-current-stack-1070/` | Gate 1 passed |
| Branch | `cursor/docs-1070-refactoring-rules-f458` | created (cloud prefix required; Constitution form would be `docs/1070_refactoring_rules_current_stack`) |
| Tasks | `tasks.md` | 0/N complete |
| Commits | bd65ae00 docs(rules): rewrite refactoring.md for current Boot 4.1/Next.js stack<br>96a8a46b test(scripts): guard refactoring.md against obsolete stack markers<br>ec0acb02 chore(openspec): archive completed fix-reportes-500-1062 change | recorded |
| Pull Request | https://github.com/matiaspakua/notaire/pull/1113 | open (draft) |
| CI run | | pending |
| Merge commit | | pending |
| Release / tag | | pending |
| Smoke test | | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Obsolete package root rejected | `bash scripts/check-agent-rules.sh` (refactoring.md markers) | pending |
| Swing-as-target markers rejected | `bash scripts/check-agent-rules.sh` | pending |
| Obsolete Boot/Java/Postgres markers rejected | `bash scripts/check-agent-rules.sh` | pending |
| Current stack markers accepted | `bash scripts/check-agent-rules.sh` after rewrite | pending |
| Existing empty/dead-path checks still pass | `bash scripts/check-agent-rules.sh` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/rules/refactoring.md` | pending | |
| `docs/300-development/DEVELOPMENT-PLAN.md` | pending | |
| `docs/200-architecture/201-SAD/sad.md` | pending | |
| `README.md` | pending | |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `openspec validate --strict` + `validate-sdlc-plan.sh` exit 0 |
| 2 | Failing tests written, test cases designed | yes | red then green `check-agent-rules.sh` + AgentRulesTest |
| 3 | Suite green, coverage held, docs updated | pending | |
| 4 | CI green, review approved, no conflicts | pending | |
| 5 | Deployed, smoke test passed, Issue closed | pending | |

## Exceptions

None. Label `in-progress` could not be applied (`gh` GraphQL: Resource not accessible by integration); recorded here, not treated as a Constitution §12 process exception.
