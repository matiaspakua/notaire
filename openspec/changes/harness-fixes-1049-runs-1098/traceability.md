# Traceability

> Governed by [CONSTITUTION.md](../../../CONSTITUTION.md) — P4 Traceability.
> This is the change's ledger. Rows below Tasks stay `pending` until the step
> actually happens.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1098 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/harness-fixes-1049-runs-1098/` | written |
| Branch | `chore/1098_harness_fixes_1049_runs` | created |
| Tasks | `tasks.md` | pending |
| Commits | pending | pending |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The spec gate removes specs a skip_specs change left behind (1 scenario) | replay on the stored #1049 spec | pending |
| The spec gate unticks tasks outside groups 1-2 (2 scenarios) | `local-ai/sdlc/tests/test_ledger.py` | pending |
| Markdown repair fixes fence languages and table pipes (2 scenarios) | `local-ai/sdlc/tests/test_md_repair.py` | pending |
| The worker's recursive searches skip dependency trees (1 scenario) | `local-ai/sdlc/tests/test_crawl_guard.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | pending | pending |
| `local-ai/README.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1098, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness
  and its setup script. The tests are Python `unittest` suites; the `foreman.sh`
  wiring is shell glue checked by `bash -n` and a replay on the stored #1049 files.
- The crawl guard was implemented before this issue existed (#1049 run); its
  tests and code land in one commit each, tests first.
