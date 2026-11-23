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
| Issue | #2411 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2411-stage-kind/` | written |
| Branch | `feat/2411_line_stage_kind` | created |
| Tasks | `tasks.md` | groups 1-8 complete |
| Commits | 1b648557, e2644063, 5d7b23cf, 7eb16b7c, f936343f, 1feef790, 994a41e6, f852a5da | done |
| Pull Request | #<!-- number --> | pending |
| CI run | <!-- workflow run URL --> | pending |
| Merge commit | <!-- SHA --> | pending |
| Release / tag | <!-- vX.Y.Z --> | pending |
| Smoke test | <!-- environment + result --> | pending |

## Requirement coverage

<!-- Every `#### Scenario:` in the delta spec is an Acceptance Criterion and must
     appear here with the test that proves it. Requirement coverage is not code
     coverage: a green suite with an unmapped scenario means the criterion is
     unverified. -->

| Scenario (Acceptance Criterion) | Test | Status |
|---------------------------------|------|--------|
| declared kind is returned in the line | `framework/ai-sdlc/tests/test_stage_kind.py` `test_declared_kind_is_returned_in_line` | passing |
| default kind is "agent" when stage has agents | `framework/ai-sdlc/tests/test_stage_kind.py` `test_default_kind_agent_when_agents_present` | passing |
| default kind is "human" when stage id is "merge" | `framework/ai-sdlc/tests/test_stage_kind.py` `test_default_kind_human_for_merge` | passing |
| default kind is "deterministic" for other stages | `framework/ai-sdlc/tests/test_stage_kind.py` `test_default_kind_deterministic_for_other_stages` | passing |
| invalid kind is a profile error | `framework/ai-sdlc/tests/test_stage_kind.py` `test_invalid_kind_raises_profile_error` | passing |
| stage_dicts includes kind | `framework/ai-sdlc/tests/test_stage_kind.py` `test_stage_dicts_includes_kind` | passing |
| gate-only stage with no agents gets deterministic default | `framework/ai-sdlc/tests/test_stage_kind.py` `test_gate_only_stage_gets_deterministic` | passing |

## Permanent documentation updated

<!-- Mirror of proposal.md — Documentation Impact, confirmed at Gate 3.
     Reference the documents; do not restate their content. -->

| Document | Updated | Commit |
|----------|---------|--------|
| <!-- path --> | <!-- yes/no --> | <!-- SHA --> |

## Gate log

| Gate | Condition | Passed | Evidence |
|------|-----------|--------|----------|
| 1 | Issue + Specification + Acceptance Criteria | <!-- --> | <!-- --> |
| 2 | Failing tests written, test cases designed | <!-- --> | <!-- --> |
| 3 | Suite green, coverage held, docs updated | <!-- --> | <!-- --> |
| 4 | CI green, review approved, no conflicts | <!-- --> | <!-- --> |
| 5 | Deployed, smoke test passed, Issue closed | <!-- --> | <!-- --> |

## Exceptions

None
