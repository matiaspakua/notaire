# Traceability

> Governed by [CONSTITUTION.md](../../../../CONSTITUTION.md) — P4 Traceability.
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
| Issue | #2404 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/fix-2404-line-stage-metadata/` | passed Gate 1 |
| Branch | `fix/2404_line_stage_metadata` | active |
| Tasks | `tasks.md` | 9/12 complete (groups 1-6, 8-9 done; 10-12 require PR) |
| Commits | `77142974`, `b4ea6af0`, `d477d709`, `a5771c19`, `6144d0d1` | done |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Stage always carries all keys with a configured stage | `TestStageMetadataKeys.test_every_stage_has_all_required_keys` | passed |
| Gate-only stage carries empty values | `TestGateOnlyStages.test_signal_stage_empty_defaults`, `test_merge_stage_empty_defaults` | passed |
| Stage guardrails list grants of the stage | `TestStageGuardrails.test_plan_guardrails_has_grant_with_kind`, `test_pre_push_guardrails_has_own_grant` | passed |
| Real profile shape with agents, skills, model, effort and mcp | `TestConfiguredStages.test_plan_stage_agents_skills_mcp_model_effort`, `test_red_stage_values` | passed |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/CHANGELOG.md` | yes | `6144d0d1` |
| `framework/ai-sdlc/README.md` | yes | `6144d0d1` |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | yes | `6144d0d1` |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec Gate 1 commit; strict OpenSpec and SDLC plan validation |
| 2 | Failing tests written, test cases designed | yes | `d477d709` — 13 tests fail with KeyError/AssertionError |
| 3 | Suite green, coverage held, docs updated | yes | 738 passed, 4 skipped; docs in `6144d0d1` |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
