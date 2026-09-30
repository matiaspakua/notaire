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
| Issue | #1099 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `openspec/changes/gpt-oss-preset-1099/` | written |
| Branch | `chore/1099_gpt_oss_preset` | created |
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
| The setup script installs a model by preset (1 scenario) | manual `PRESET=gpt-oss` run | pending |
| gpt-oss tool calls are repaired before Codex sees them (4 scenarios) | `local-ai/sdlc/tests/test_harmony_repair.py` | pending |
| The oMLX patch is idempotent and reversible (2 scenarios) | `local-ai/sdlc/tests/test_patch_omlx.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | pending | pending |
| `local-ai/README.md` | pending | pending |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1099, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | pending | pending |
| 3 | Suite green, coverage held, docs updated | pending | pending |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI setup.
  The repair and patcher are covered by Python `unittest` suites; the preset
  wiring is shell, checked by `bash -n` and a real `PRESET=gpt-oss` run.
