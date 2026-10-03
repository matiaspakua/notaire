# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #1047 | open (implement in progress; label ACL 403) |
| Use Case | CU74 – Performance and Caching Strategy; CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #594 CLOSED (added k6); `b822a18` deleted script; #303 docs; audit-2026-09; serialize after #1057/#1048 | referenced |
| Specification | `openspec/changes/fix-1047-k6-load-test/` | Gate 1 in repo; validate PASS |
| Branch | `cursor/fix-1047-k6-load-test-69d3` | created from origin/main after #1048 (`2967ec32`) |
| Tasks | `tasks.md` | Gate 1 planning complete; implement pending |
| Commits | `aa418f3f`, `0312f01e`, `64b31fe0`, `82b21a30`, `09c25b0c`, `e2fffe26` | landed on branch |
| Pull Request | [#1154](https://github.com/matiaspakua/notaire/pull/1154) | draft |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| k6 script file exists at workflow path | `test_performance_test_assets.py` + path assert | local green |
| Script uses English login DTO fields | asset unittest (`name`/`password`) | local green |
| Script declares stages and SLO thresholds | asset unittest (stages/thresholds/http_req_*) | local green |
| Script covers gestiones/presupuestos/tramites with Bearer auth | asset unittest | local green |
| Script writes summary artifact output | asset unittest (`handleSummary` / `summary.json`) | local green |
| Workflow runs k6 against live stack on schedule/dispatch | workflow YAML assert (existing + artifact path) | local green |
| Asset unittest suite green | `python3 scripts/test_performance_test_assets.py` | local green |
| Workflow green on manual dispatch (post-merge smoke) | Actions `workflow_dispatch` | pending |
| Results published as artifact | upload-artifact finds `summary.json` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `docs/100-business/102-use-cases/CU74 – Performance and Caching Strategy.md` | yes | `82b21a30` |
| `docs/100-business/102-use-cases/CU76 – Quality Assurance and Testing Infrastructure.md` | yes | `82b21a30` |
| Related perf docs / #303 pointers (if stale) | n/a (arch docs already reference workflow) | — |
| `CHANGELOG.md` | yes | `82b21a30` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes (draft) | artifacts in this folder; validate via temp copy into `openspec/changes/` |
| 2 | Failing tests written, test cases designed | yes | extended asset tests FileNotFoundError + workflow ignore fail before restore |
| 3 | Suite green, coverage held, docs updated | local | `python3 scripts/test_performance_test_assets.py` OK; docs/CHANGELOG updated |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

Issue `in-progress` label: GraphQL ACL 403 for integration token (same as prior
fleet notes). Implement started after #1048 squash-merge `2967ec32` on main.
