# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.

## Chain

```text
Issue → Specification → Tasks → Commits → PR → Merge → Release
```

| Link | Reference | Status |
|------|-----------|--------|
| Issue | #1377 | open → in progress |
| Use Case | CU76 – Quality Assurance and Testing Infrastructure | exists |
| Related | #1099, #1107 | referenced |
| Specification | `docs/openspec/changes/feat-1377-ternary-bonsai-preset/` | Gate 1 draft |
| Branch | `feat/1377_add-ternary-bonsai-local-ai-model` | created from `main` |
| Tasks | `tasks.md` | in progress |
| Commits | see branch | in progress |
| Pull Request | — | pending |
| CI run | — | pending |
| Merge commit | — | pending |
| Release / tag | — | pending |
| Smoke test | — | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Existing presets are unchanged | `local-ai/sdlc/tests/test_models_config.py` | passing |
| The Bonsai preset is available | `local-ai/sdlc/tests/test_models_config.py`, `test_omlx_bonsai_integration.py` | passing (live model case skipped: model not downloaded) |
| Unknown models fail | `local-ai/sdlc/tests/test_models_config.py` | passing |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `local-ai/sdlc/AI-SDLC.md` | pending | — |
| `CHANGELOG.md` | pending | — |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | `bash workspace/sdlc/validate-sdlc-plan.sh feat-1377-ternary-bonsai-preset` |
| 2 | Failing tests written, test cases designed | yes | the resolver tests import `models_config`, which does not exist on the base commit |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
