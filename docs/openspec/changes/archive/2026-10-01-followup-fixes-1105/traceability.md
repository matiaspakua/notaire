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
| Issue | #1105 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/followup-fixes-1105/` | written |
| Branch | `chore/1105_followup_fixes` | created |
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `450e899` (plan) … final archive commit; tests first: `36271b2` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| A lost gpt-oss tool call becomes a recovery call (2 scenarios) | `test_harmony_repair.py`, `test_patch_omlx.py`; real harmony parser in oMLX | green |
| The uncommitted-changes gate names the staging command (1 scenario) | #1049 implement replay: the worker ran the named command and passed | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/README.md` | yes | `af40f89` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1105, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `36271b2` failing before `b762743` |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green; real `openai_harmony` in oMLX: lost call gives the echo, a good call and a final answer are unchanged; #1049 implement then passed with the recovery live; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI setup and
  harness. The repair and patcher have Python `unittest` suites; the `with_retries`
  check is shell, verified by `bash -n` and a replay of the #1049 spec phase.
