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
| Issue | #1027 | in-progress |
| Use Case | none — documented internal-tooling exception (see proposal.md, precedent #973) | n/a |
| Specification | `openspec/changes/enforce-workflow-gates-via-hooks/` (`skip_specs: true`) | drafted |
| Branch | `chore/1027_enforce_workflow_gates_via_hooks` | created |
| Tasks | `tasks.md` | in progress |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | n/a — internal tooling, no release artifact | pending |
| Smoke test | pending | pending |

## Requirement coverage

n/a — `skip_specs: true`, no delta spec scenarios for this change. Acceptance
Criteria are tracked directly in Issue #1027 and verified manually (see
design.md — Testing Strategy).

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| SessionStart hook prints OpenSpec status | Manual: new session transcript inspection | pending |
| PreToolUse hook blocks `git push` to `main`/`master` | Manual: attempted push denial transcript | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `CLAUDE.md` | pending | pending |
| `.claude/rules/hooks.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | Issue #1027; `openspec/changes/enforce-workflow-gates-via-hooks/proposal.md` |
| 2 | Failing tests written, test cases designed | n/a (documented exception — no test-able application code; manual verification plan in design.md) | design.md — Testing Strategy |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

- **TDD (Gate 2) / automated test suite**: this change touches only
  `.claude/settings.json` (agent-harness hook configuration) and Markdown
  documentation — there is no unit/integration/E2E test harness in this repo
  for Claude Code hooks. Per CONSTITUTION.md §12, exceptions require explicit
  human approval; this is recorded here for the user's review rather than
  self-approved. Manual verification (documented in the PR with actual
  transcript output) substitutes for automated tests, per design.md — Testing
  Strategy.
- **No Use Case**: this is a purely internal process/tooling change with no
  business behavior. Documented per the precedent set in issue #973's body
  (a purely technical epic with no Use Case).
