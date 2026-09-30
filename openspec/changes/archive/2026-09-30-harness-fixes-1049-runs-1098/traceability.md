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
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `5ba275a` (plan) … final archive commit; tests first: `bca9c83` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The spec gate removes specs a skip_specs change left behind (1 scenario) | replay: stored #1049 Qwen spec validates once `specs/` is removed | green |
| The spec gate unticks tasks outside groups 1-2 (2 scenarios) | `local-ai/sdlc/tests/test_ledger.py` | green |
| Markdown repair fixes fence languages and table pipes (2 scenarios) | `local-ai/sdlc/tests/test_md_repair.py` | green |
| The worker's recursive searches skip dependency trees (1 scenario) | `local-ai/sdlc/tests/test_crawl_guard.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | yes | `66b640d` |
| `local-ai/README.md` | yes | `49b272a` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1098, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `bca9c83` committed and failing (no `untick_after`, no `md_repair`) before `5b2f0b8` |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green (78); `RECHECK=1` replay of the stored #1049 gpt-oss spec: 37 premature ticks unticked, MD040/MD055 repaired, all spec gates PASS; Codex run with the new flags lists only `src/` for `grep -rl`; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI harness
  and its setup script. The tests are Python `unittest` suites; the `foreman.sh`
  wiring is shell glue checked by `bash -n` and a replay on the stored #1049 files.
- The crawl guard was implemented before this issue existed (#1049 run); its
  tests and code land in one commit each, tests first.
