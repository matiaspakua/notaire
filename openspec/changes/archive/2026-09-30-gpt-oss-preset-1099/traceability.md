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
| Tasks | `tasks.md` | groups 1-9 done; 10-12 at PR/merge |
| Commits | `b61148d` (plan) … final archive commit; tests first: `cb711af`, `9b6eb48` | done |
| Pull Request | pending | pending |
| CI run | pending | pending |
| Merge commit | pending | pending |
| Release / tag | pending | pending |
| Smoke test | pending | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| The setup script installs a model by preset (1 scenario) | `PRESET=gpt-oss` run regenerated `omlx-gptoss` profile and catalog; `omlx` profile and catalog md5 unchanged | green |
| gpt-oss tool calls are repaired before Codex sees them (4 scenarios) | `local-ai/sdlc/tests/test_harmony_repair.py` | green |
| The oMLX patch is idempotent and reversible (2 scenarios) | `local-ai/sdlc/tests/test_patch_omlx.py` | green |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | yes | `bdea2a6` |
| `local-ai/README.md` | yes | `bdea2a6` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | #1099, this folder, `validate-sdlc-plan.sh` green |
| 2 | Failing tests written, test cases designed | yes | `cb711af` failing (no modules) before `4abf33e`; `9b6eb48` failing (invalid escape) before the repair |
| 3 | Suite green, coverage held, docs updated | yes | harness self-tests green (93); patcher on the real oMLX 0.7.0 file: 4 patches applied, rerun `already`, compiles; coding smoke 7/8 with `omlx-gptoss` (before: 6/8); default-preset rerun is a no-op; `preflight.sh` passed |
| 4 | CI green, review approved, no conflicts | pending | pending |
| 5 | Deployed, smoke test passed, Issue closed | pending | pending |

## Exceptions

- No Playwright or backend tests: the change touches only the local-AI setup.
  The repair and patcher are covered by Python `unittest` suites; the preset
  wiring is shell, checked by `bash -n` and a real `PRESET=gpt-oss` run.
