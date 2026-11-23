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
| Issue | #2410 | in-progress |
| Use Case | CU76 — Quality Assurance and Testing Infrastructure | exists |
| Specification | `docs/openspec/changes/feat-2410-workflow-steps/` | <!-- --> |
| Branch | `feat/2410_workflow_steps` | <!-- --> |
| Tasks | `tasks.md` | groups 1-8 complete |
| Commits | 0a3ba474, c6d6b30f, 93f4269a, 9949fe36, a4ddd743, bb6c18f4, 7c3b834f | done |
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
| Steps with stages are saved and the workflow returns to draft | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_steps_with_stages_are_saved_as_a_draft` | passing |
| A step without a stage is refused | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_a_step_without_a_stage_is_refused` | passing |
| Duplicate step ids are refused | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_duplicate_step_ids_are_refused` | passing |
| An approved workflow is immutable | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_an_approved_workflow_is_immutable` | passing |
| The API needs If-Match with the current version | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_put_needs_the_current_version` | passing |
| Status shows the step's stage | `framework/ai-sdlc/tests/test_workflow_steps_edit.py` `test_status_reports_the_steps_stage` | passing |

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

None. The factory-feature approval uses the Owner override on the Owner's instruction (2026-11-23).
