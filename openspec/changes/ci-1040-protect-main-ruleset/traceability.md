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
| Issue | #1040 | open (implement in progress; label ACL 403) |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure; CU78 – Security and Compliance | exists |
| Related | audit-2026-09; #1041 merged (`6b246a72`); queue `#1046 → #1042 → #1041 → #1040` | done predecessors |
| Specification | `openspec/changes/ci-1040-protect-main-ruleset/` | Gate 1 validated |
| Branch | `cursor/ci-1040-protect-main-ruleset-69d3` | created from `origin/main` |
| Tasks | `tasks.md` | implement mostly complete; Gate 4/5 pending |
| Commits | — | pending (filled after commit) |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending (coordinator) |
| Release / tag | — | n/a |
| Smoke test | admin `--apply` + assert + direct-push reject | pending Gate 5 |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Ruleset requires pull request | `scripts/test_protect_main_ruleset.py` + `assert-protect-main-ruleset.sh` | in-repo green; live pending admin apply |
| Ruleset lists five exact check names | same | in-repo green; live pending admin apply |
| Aggregator jobs publish those check names | same (YAML) | green |
| Non-fast-forward and deletion rules present | same | in-repo green; live keep/extend |
| No durable bot bypass after #1041 | same | desired empty; live empty |
| hooks.md gap text updated | same (string assert) | green |
| Live apply + direct-push smoke | admin checklist / Gate 5 | pending after merge |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `.claude/rules/hooks.md` | yes | pending SHA |
| `docs/200-architecture/208-devsecops/README.md` | yes | pending SHA |
| `docs/github/PRODUCTION-READINESS-AUDIT-2026-09.md` | yes (brief theme) | pending SHA |
| `CHANGELOG.md` | yes | pending SHA |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash scripts/validate-sdlc-plan.sh ci-1040-protect-main-ruleset` |
| 2 | Failing tests written, test cases designed | yes | observed FAIL on pre-change tree; then green |
| 3 | Suite green, coverage held, docs updated | yes (local scripts) | `test_protect_main_ruleset.py` + related workflow tests |
| 4 | CI green, review approved, no conflicts | pending | heavy CI / coordinator |
| 5 | Deployed, smoke test passed, Issue closed | pending | admin `apply-protect-main-ruleset.sh --apply` |

## Exceptions

- Playwright product specs: n/a — no UI surface; PR must still pass heavy CI Playwright job.
- Issue `in-progress` label: integration token GraphQL 403 (expected).
- Live ruleset apply: admin-only; not performed by this agent.
