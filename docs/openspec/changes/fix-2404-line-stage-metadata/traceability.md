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
| Tasks | `tasks.md` | 1/12 complete |
| Commits | <!-- SHAs, one per line --> | pending |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| Stage always carries all keys with a configured stage | `tests/test_line_stage_metadata.py` | pending |
| Gate-only stage carries empty values | `tests/test_line_stage_metadata.py` | pending |
| Stage guardrails list grants of the stage | `tests/test_line_stage_metadata.py` | pending |
| Real profile shape with agents, skills, model, effort and mcp | `tests/test_line_stage_metadata.py` | pending |

## Permanent documentation updated

| Document | Updated | Commit |
|----------|---------|--------|
| `framework/ai-sdlc/CHANGELOG.md` | <!-- yes/no --> | <!-- SHA --> |
| `framework/ai-sdlc/README.md` | <!-- yes/no --> | <!-- SHA --> |
| `framework/ai-sdlc/openapi/factory-control.openapi.yaml` | <!-- yes/no --> | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | yes | OpenSpec Gate 1 commit; strict OpenSpec and SDLC plan validation |
| 2 | Failing tests written, test cases designed | pending | — |
| 3 | Suite green, coverage held, docs updated | pending | — |
| 4 | CI green, review approved, no conflicts | pending | — |
| 5 | Deployed, smoke test passed, Issue closed | pending | — |

## Exceptions

None.
